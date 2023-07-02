
import db from '../database/database.js';
import { collection, addDoc, getDocs, doc, getDoc, updateDoc, deleteDoc, query, where } from "firebase/firestore";


export const presentation = (request, response) => {

    response.send('Servidor de UrBus inicializado correctamente');

};

export const addUser = async (request, response) => {
    const data = request.body;

    try {
        if (data.userName != "" || data.userName != undefined || data.userId != "" || data.userId != undefined || data.userEmail != "" || data.userEmail != undefined) {
            const docRef = await addDoc(collection(db, "users"), { userId: data.userId, userName: data.userName, userEmail: data.userEmail});
            response.status(200).json("User successfully added with ID " + docRef.id);
        }
        else {
            response.status(400).json({ message: "Data not defined" });
        }

    } catch (error) {
        response.status(400).json({ message: error.message });
    }

};


export const getAllUsers = async (request, response) => {
    try {
        const querySnapshot = await getDocs(collection(db, "users"));
        const users = [];
        querySnapshot.forEach((doc) => {
            users.push({
                id: doc.id,
                data: doc.data(),
            });
        });
        response.status(200).json(users);

    } catch (error) {
        response.status(404).json({ message: error.message });
    }

};

export const getUser = async (request, response) => {
    try {
        const userId = request.params.id; 
        const usersRef = collection(db, "users");
        const querySnapshot = await getDocs(query(usersRef, where("userId", "==", userId)));

        if (querySnapshot.empty) {
            return response.status(404).send("User doesn't exist");
        }

        const user = querySnapshot.docs[0].data();

        response.status(200).json(user);

    } catch (error) {
        response.status(500).json({ message: error.message });
    }
};


export const editUser = async (request, response) => {
    try {
        const userId = request.params.id;
        const usersRef = collection(db, "users");
        const querySnapshot = await getDocs(query(usersRef, where("userId", "==", userId)));

        if (querySnapshot.empty) {
            return response.status(404).send("User doesn't exist");
        }

        const userDoc = querySnapshot.docs[0];
        const userRef = doc(db, "users", userDoc.id);

        const { userName, userEmail } = request.body;

        await updateDoc(userRef, {
            userName,
            userEmail
        });

        const updatedUserDoc = await getDoc(userRef);
        const updatedUser = {
            userId: updatedUserDoc.data().userId,
            userName: updatedUserDoc.data().userName,
            userEmail: updatedUserDoc.data().userEmail
        };

        response.status(200).json(updatedUser);
    } catch (error) {
        response.status(500).json({ message: error.message });
    }
};

export const deleteUser = async (request, response) => {
    try {
        const userId = request.params.id;
        const usersRef = collection(db, "users");
        const querySnapshot = await getDocs(query(usersRef, where("userId", "==", userId)));

        if (querySnapshot.empty) {
            return response.status(404).send("User doesn't exist");
        }

        const userDoc = querySnapshot.docs[0];
        const userRef = doc(db, "users", userDoc.id);

        await deleteDoc(userRef);

        response.status(200).send("User deleted successfully");
    } catch (error) {
        response.status(500).json({ message: error.message });
    }
};