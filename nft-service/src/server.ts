import express, { Request, Response } from 'express';
import { createAloyCollection, createProfileBadge, updateProfileBadge, createGeneralBadge } from './nftService';
import * as bp from 'body-parser';

// Define the port number
const PORT = 3000;

// Create Express app
const app = express();
app.use(express.json());

// Define a route handler for the root path
app.get('/', (req: Request, res: Response) => {
  res.send('Hello, NFT Service!');
});

app.get('/collection', (req: Request, res: Response) => {
  createAloyCollection().then((collectionAddress) => {
    res.send(`Collection NFT: [${collectionAddress}]`)
  }, (error) => {
    res.send(`Error: ${error}`)
  }
  );
});

app.post('/profileNft', (req: Request, res: Response) => {
  createProfileBadge(req.body.badgeData, req.body.receiverAddress).then((nftResponse) => {
    res.send(nftResponse)
  }, (error) => {
    res.send({ error: error })
  });
});

app.put('/profileNft', (req: Request, res: Response) => {
  updateProfileBadge(req.body.badgeData).then((nftResponse) => {
    res.send(nftResponse)
  }, (error) => {
    res.send({ error: error })
  });
});

app.post('/badge', (req: Request, res: Response) => {
  createGeneralBadge(req.body.badgeData, req.body.receiverAddress).then((nftResponse) => {
    res.send(nftResponse)
  }, (error) => {
    res.send({ error: error })
  });
});

// Start the server
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});