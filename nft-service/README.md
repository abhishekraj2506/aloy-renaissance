# NFT-Service

The **NFT-Service** is a vital component of our platform, tasked with the creation and distribution of **profile NFTs and experience badges** to our users. This service operates by minting unique NFTs within a common *Aloy Collection*, subsequently transferring them to the designated user wallet addresses. It leverages the robust capabilities of Metaplex for the seamless creation and management of NFTs.

## Key Functions

### NFT Generation
The core function of the NFT-Service revolves around the generation of distinct profile NFTs and experience badges tailored to each user. These NFTs serve as digital representations of user achievements and milestones within our ecosystem.

### Aloy Collection Integration
As part of our commitment to offering unparalleled digital assets, the NFT-Service integrates seamlessly with the esteemed *Aloy Collection*. This collaboration ensures that our users receive NFTs of exceptional quality and value.

### Metaplex Utilization
Internally, the NFT-Service harnesses the power of Metaplex, a leading platform for creating and managing NFTs.

## Communication Protocol
Currently, the NFT-Service communicates with the core-service via HTTP endpoints.

## Future Enhancements

- **Explore cNFTs and Token Extensions**: We want to integrate Token Extensions to solve for KYC and identity token, as well as use metadata extensions to add more relevant details onchain.
- **Enhanced User Interface**: Develop a user-friendly interface for seamless NFT browsing and management.
- **Async Comms**: Move to a producer-consumer paradigm as minting NFTs can be an async operation.
