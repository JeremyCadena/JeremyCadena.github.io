import express from 'express';
import dotenv from 'dotenv';
import Routes from './routes/routes.js';
import cors from 'cors';
import bodyParser from 'body-parser';

const app = express();
dotenv.config();

app.use(bodyParser.json({extended: true}));
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cors());
app.use('/urbus',Routes);

const port = process.env.PORT || 8000;

app.listen(port, function() {console.log('listening on port ' + port);});