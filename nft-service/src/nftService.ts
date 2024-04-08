import {
    Metaplex,
    NftWithToken,
    bundlrStorage,
    keypairIdentity,
    toMetaplexFile,
} from "@metaplex-foundation/js"
import { createAssociatedTokenAccountInstruction, createTransferCheckedInstruction, getAssociatedTokenAddressSync } from "@solana/spl-token"
import { Connection, Keypair, PublicKey, Transaction, clusterApiUrl } from "@solana/web3.js"
import dotenv from "dotenv"
import * as fs from "fs"
dotenv.config()

const connection = new Connection(clusterApiUrl("devnet"))
const secret = JSON.parse(process.env.PRIVATE_KEY ?? "") as number[]
const user = Keypair.fromSecretKey(Uint8Array.from(secret))
console.log("PublicKey:", user.publicKey.toBase58())
// metaplex set up
const metaplex = Metaplex.make(connection)
    .use(keypairIdentity(user))
    .use(
        bundlrStorage({
            address: "https://devnet.bundlr.network",
            providerUrl: "https://api.devnet.solana.com",
            timeout: 60000,
        }),
    );
const collectionNftPublicKey = new PublicKey(process.env.ALOY_COLLECTION_NFT_KEY ?? "");

interface NftData {
    name: string
    symbol: string
    description: string
    sellerFeeBasisPoints: number
    imageFile: string
    attributes?: Array<{
        trait_type?: string;
        value?: string;
    }>
}

interface NftUpdateData {
    nftAddress: string,
    attributes?: Array<{
        trait_type?: string;
        value?: string;
    }>
}

interface NftResponse {
    metadataUri: string,
    nftAddress: string,
    nftUrl: string
}

interface CollectionNftData {
    name: string
    symbol: string
    sellerFeeBasisPoints: number
}


const collectionNftData = {
    name: process.env.NFT_COLLECTION_NAME?.toString() || "Aloy Test Collection",
    symbol: process.env.NFT_COLLECTION_SYMBOL?.toString() || "ALTST",
    description: process.env.NFT_COLLECTION_DESCRIPTION?.toString() || "Aloy Collection Description",
    sellerFeeBasisPoints: 0,
    imageFile: "aloy_collection.jpg",
    isCollection: true,
}

// helper function to upload image and metadata
async function uploadMetadata(
    metaplex: Metaplex,
    nftData: NftData,
    imageUri: string | null = null
): Promise<string> {

    if (!imageUri) {
        console.log("No static image uri provided, uploading image...")
        const buffer = fs.readFileSync("images/" + nftData.imageFile);

        // buffer to metaplex file
        const file = toMetaplexFile(buffer, nftData.imageFile);

        // upload image and get image uri
        imageUri = await metaplex.storage().upload(file);
        console.log("Uploaded image uri:", imageUri);
    }


    // upload metadata and get metadata uri (off chain metadata)
    const { uri } = await metaplex.nfts().uploadMetadata({
        name: nftData.name,
        symbol: nftData.symbol,
        description: nftData.description,
        attributes: nftData.attributes,
        image: imageUri,
    });

    console.log("metadata uri:", uri);
    return uri;
}

// helper function create NFT
async function createNft(
    metaplex: Metaplex,
    uri: string,
    nftData: NftData,
    collectionMint: PublicKey,
    customerWalletAddress: string
): Promise<NftWithToken> {
    const { nft } = await metaplex.nfts().create(
        {
            uri: uri, // metadata URI
            name: nftData.name,
            sellerFeeBasisPoints: nftData.sellerFeeBasisPoints,
            symbol: nftData.symbol,
            collection: collectionMint,
            tokenOwner: user.publicKey,

        },
        { commitment: "finalized" },
    );

    console.log(
        `Token Mint: https://explorer.solana.com/address/${nft.address.toString()}?cluster=devnet`,
    );

    //this is what verifies our collection as a Certified Collection
    await metaplex.nfts().verifyCollection({
        mintAddress: nft.mint.address,
        collectionMintAddress: collectionMint,
        isSizedCollection: true,
    })

    const customerPubKey = new PublicKey(customerWalletAddress)

    const customerTokenAddress = getAssociatedTokenAddressSync(nft.mint.address, customerPubKey, true)
    console.log(`Customer token address: ${customerTokenAddress.toString()}`)
    const userTokenAddress = getAssociatedTokenAddressSync(nft.mint.address, user.publicKey)
    console.log(`User token address: ${userTokenAddress.toString()}`)
    const createIx = createAssociatedTokenAccountInstruction(
        user.publicKey,
        customerTokenAddress,
        customerPubKey,
        nft.mint.address,
    )

    const transferIx = createTransferCheckedInstruction(
        userTokenAddress,
        nft.mint.address,
        customerTokenAddress,
        user.publicKey,
        1,
        0,
        [],
    )
    const blockHash = await connection.getLatestBlockhash()
    const tx = new Transaction({ feePayer: user.publicKey, recentBlockhash: blockHash.blockhash })
    tx.add(createIx, transferIx)
    tx.sign(user)
    const txnSignature = await connection.sendRawTransaction(tx.serialize())
    await connection.confirmTransaction({
        blockhash: blockHash.blockhash, lastValidBlockHeight: blockHash.lastValidBlockHeight,
        signature: txnSignature
    })
    console.log(`Nft transfet txn signature ${txnSignature}`)
    return nft;
}

// helper function update NFT
async function updateNftUri(
    metaplex: Metaplex,
    uri: string,
    mintAddress: PublicKey,
) {
    // fetch NFT data using mint address
    const nft = await metaplex.nfts().findByMint({ mintAddress });

    // update the NFT metadata
    const { response } = await metaplex.nfts().update(
        {
            nftOrSft: nft,
            uri: uri,
        },
        { commitment: "finalized" },
    );

    console.log(
        `Token Mint: https://explorer.solana.com/address/${nft.address.toString()}?cluster=devnet`,
    );

    console.log(
        `Transaction: https://explorer.solana.com/tx/${response.signature}?cluster=devnet`,
    );
}

async function createCollectionNft(
    metaplex: Metaplex,
    uri: string,
    data: CollectionNftData
): Promise<NftWithToken> {
    const { nft } = await metaplex.nfts().create(
        {
            uri: uri,
            name: data.name,
            sellerFeeBasisPoints: data.sellerFeeBasisPoints,
            symbol: data.symbol,
            isCollection: true,
        },
        { commitment: "finalized" }
    )

    console.log(
        `Collection Mint: https://explorer.solana.com/address/${nft.address.toString()}?cluster=devnet`
    )

    return nft
}

export async function createAloyCollection(): Promise<string> {

    // upload data for the collection NFT and get the URI for the metadata
    const collectionUri = await uploadMetadata(metaplex, collectionNftData, process.env.ALOY_COLLECTION_IMAGE_URI || null)

    // create a collection NFT using the helper function and the URI from the metadata
    const collectionNft = await createCollectionNft(
        metaplex,
        collectionUri,
        collectionNftData
    )
    console.log(`Collection NFT: [${collectionNft.address.toString()}]`)
    return collectionNft.address.toString()
}

function getBasicProfileNftData(badgeData: NftData | NftUpdateData) {
    return {
        name: process.env.NFT_NAME?.toString() || "Aloy-Tst-Card",
        symbol: process.env.NFT_SYMBOL?.toString() || "ATC",
        description: "Aloy Member Card",
        sellerFeeBasisPoints: 0,
        imageFile: "aloy_member.jpg",
        attributes: badgeData.attributes,
    }
}

export async function createProfileBadge(badgeData: NftData, customerWalletAddress: string): Promise<NftResponse> {
    console.log("Creating profile badge for customer wallet address:", customerWalletAddress)
    let nftData = getBasicProfileNftData(badgeData)
    const metadataUri = await uploadMetadata(metaplex, nftData, process.env.ALOY_PROFILE_CARD_IMAGE_URI || null)
    const nft = await createNft(metaplex, metadataUri, nftData, collectionNftPublicKey, customerWalletAddress)
    const nftAddress = nft.address.toString()

    return {
        metadataUri: metadataUri,
        nftAddress: nftAddress,
        nftUrl: `https://explorer.solana.com/address/${nft.address.toString()}?cluster=devnet`
    }
}

export async function updateProfileBadge(badgeData: NftUpdateData): Promise<NftResponse> {
    //  upload updated NFT data and get the new URI for the metadata
    let nftData = getBasicProfileNftData(badgeData)
    const updatedUri = await uploadMetadata(metaplex, nftData, process.env.ALOY_PROFILE_CARD_IMAGE_URI || null)

    // // update the NFT using the helper function and the new URI from the metadata
    await updateNftUri(metaplex, updatedUri, new PublicKey(badgeData.nftAddress))
    return {
        metadataUri: updatedUri,
        nftAddress: badgeData.nftAddress,
        nftUrl: `https://explorer.solana.com/address/${badgeData.nftAddress.toString()}?cluster=devnet`
    }
}

function getBadgeNftData(badgeData: NftData) {
    return {
        name: badgeData.name,
        symbol: process.env.NFT_SYMBOL?.toString() || "ATC",
        description: badgeData.description,
        sellerFeeBasisPoints: 0,
        imageFile: badgeData.imageFile,
        attributes: badgeData.attributes,
    }
}

export async function createGeneralBadge(badgeData: NftData, customerWalletAddress: string): Promise<NftResponse> {
    console.log("Creating badge for customer wallet address:", customerWalletAddress)
    let nftData = getBadgeNftData(badgeData)
    let imageUri = null
    if (badgeData.imageFile == 'explorer.png') {
        imageUri = 'https://arweave.net/-dCHvewR065Zc2ewfPnrWm0BBtWqGsLM_a1NiIOTLjc'
    }
    console.log("Image URI:", imageUri)
    const metadataUri = await uploadMetadata(metaplex, nftData, imageUri)
    const nft = await createNft(metaplex, metadataUri, nftData, collectionNftPublicKey, customerWalletAddress)
    const nftAddress = nft.address.toString()

    return {
        metadataUri: metadataUri,
        nftAddress: nftAddress,
        nftUrl: `https://explorer.solana.com/address/${nft.address.toString()}?cluster=devnet`
    }
}
