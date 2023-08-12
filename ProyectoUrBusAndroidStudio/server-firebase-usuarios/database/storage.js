import { initializeApp } from "firebase/app";
import { getStorage } from "firebase/storage";

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
    const storage = getStorage(firebaseApp);
    console.log ("Conection to storage successfully");
    return storage;
  } catch (error) {
    console.log ("Conection to storage failed");    
  }

};

const storage = Conection();

export default storage;


