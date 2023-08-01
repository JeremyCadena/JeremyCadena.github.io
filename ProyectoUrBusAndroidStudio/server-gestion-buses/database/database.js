import { initializeApp } from "firebase/app";
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "AIzaSyCxG15qhEDgr4K8HCzTMFWLWzhh-KUi6Qs",
  authDomain: "shoppasistent.firebaseapp.com",
  databaseURL: "https://shoppasistent-default-rtdb.firebaseio.com",
  projectId: "shoppasistent",
  storageBucket: "shoppasistent.appspot.com",
  messagingSenderId: "296174001527",
  appId: "1:296174001527:web:5131b3bb78f70099789572",
  measurementId: "G-H3B6Y1YFL9"
};

function Conection() {
  try {
    
    const firebaseApp = initializeApp(firebaseConfig);
    const db = getFirestore(firebaseApp);
    console.log ("Conection to firebase successfully");
    return db;
  } catch (error) {
    console.log ("Conection to firebase failed");    
  }

};

const db = Conection();

export default db;


