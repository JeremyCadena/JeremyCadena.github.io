
import db from '../database/database.js';
import { collection, addDoc, getDocs, doc, getDoc, updateDoc, deleteDoc, query, where } from "firebase/firestore";


export const presentation = (request, response) => {

    response.send('Servidor de Urbus para lineas de buses inicializado correctamente');

};

export const addBusStop = async (request, response) => {
    const data = request.body;

    try {
        if (data.BUSS_ID != "" || data.BUSS_ID != undefined || data.BUSL_ID != "" || data.BUSL_ID != undefined || data.BUSS_MAINSTREET != "" || data.BUSS_MAINSTREET != undefined || data.BUSS_NAME != "" || data.BUSS_NAME != undefined || data.BUSS_SECONDARYSTREET != "" || data.BUSS_SECONDARYSTREET != undefined || data.BUSS_NODE != "" || data.BUSS_NODE != undefined || data.BUSS_SENSE != "" || data.BUSS_SENSE != undefined) {
            const docRef = await addDoc(collection(db, "busStops"), {
                BUSS_ID: data.BUSS_ID,
                BUSL_ID: data.BUSL_ID,
                BUSS_MAINSTREET: data.BUSS_MAINSTREET,
                BUSS_NAME: data.BUSS_NAME,
                BUSS_NODE: data.BUSS_NODE,
                BUSS_SECONDARYSTREET: data.BUSS_SECONDARYSTREET,
                BUSS_SENSE: data.BUSS_SENSE,
            });
            response.status(200).json("Bus Stop successfully added with ID " + docRef.id);
        }
        else {
            response.status(400).json({ message: "Data not defined" });
        }

    } catch (error) {
        response.status(400).json({ message: error.message });
    }

};


export const getAllBusStops = async (request, response) => {
    try {
        const querySnapshot = await getDocs(collection(db, "busStops"));
        const busStops = [];
        querySnapshot.forEach((doc) => {
            const data = doc.data();
            // Limpia los valores de los campos que puedan tener el carácter "\r"
            const cleanedData = {
                id: doc.id,
                data: {
                    BUSL_ID: data.BUSL_ID.trim(),
                    BUSS_ID: data.BUSS_ID.trim(),
                    BUSS_MAINSTREET: data.BUSS_MAINSTREET.trim(),
                    BUSS_NAME: data.BUSS_NAME.trim(),
                    BUSS_NODE: data.BUSS_NODE,
                    BUSS_SECONDARYSTREET: data.BUSS_SECONDARYSTREET.trim(),
                    BUSS_SENSE: data.BUSS_SENSE.trim(),
                },
            };
            busStops.push(cleanedData);
        });
        response.status(200).json(busStops);
    } catch (error) {
        response.status(404).json({ message: error.message });
    }
};


export const getBusStop = async (request, response) => {
    try {
        const busStopId = request.params.id;
        const busStopRef = collection(db, "busStops");
        const querySnapshot = await getDocs(query(busStopRef, where("BUSS_ID", "==", busStopId)));

        if (querySnapshot.empty) {
            return response.status(404).send("Bus Stop doesn't exist");
        }

        const busStopData = querySnapshot.docs[0].data();
        // Limpia los valores de los campos que puedan tener el carácter "\r"
        const cleanedBusStop = {
            id: querySnapshot.docs[0].id,
            data: {
                BUSL_ID: busStopData.BUSL_ID.trim(),
                BUSS_ID: busStopData.BUSS_ID.trim(),
                BUSS_MAINSTREET: busStopData.BUSS_MAINSTREET.trim(),
                BUSS_NAME: busStopData.BUSS_NAME.trim(),
                BUSS_NODE: busStopData.BUSS_NODE,
                BUSS_SECONDARYSTREET: busStopData.BUSS_SECONDARYSTREET.trim(),
                BUSS_SENSE: busStopData.BUSS_SENSE.trim(),
            },
        };

        response.status(200).json(cleanedBusStop);

    } catch (error) {
        response.status(500).json({ message: error.message });
    }
};

export const getBusStopName = async (request, response) => {
    try {
        const busStopName = request.params.name;
        const busStopRef = collection(db, "busStops");
        const querySnapshot = await getDocs(query(busStopRef, where("BUSS_NAME", "==", busStopName)));

        if (querySnapshot.empty) {
            return response.status(404).send("Bus Stop doesn't exist");
        }

        const busStopData = querySnapshot.docs[0].data();
        // Limpia los valores de los campos que puedan tener el carácter "\r"
        const cleanedBusStop = {
            id: querySnapshot.docs[0].id,
            data: {
                BUSL_ID: busStopData.BUSL_ID.trim(),
                BUSS_ID: busStopData.BUSS_ID.trim(),
                BUSS_MAINSTREET: busStopData.BUSS_MAINSTREET.trim(),
                BUSS_NAME: busStopData.BUSS_NAME.trim(),
                BUSS_NODE: busStopData.BUSS_NODE,
                BUSS_SECONDARYSTREET: busStopData.BUSS_SECONDARYSTREET.trim(),
                BUSS_SENSE: busStopData.BUSS_SENSE.trim(),
            },
        };

        response.status(200).json(cleanedBusStop);

    } catch (error) {
        response.status(500).json({ message: error.message });
    }
};

export const getBusStopSense = async (request, response) => {
    try {
        const busStopName = request.params.sense;
        const busStopRef = collection(db, "busStops");
        const querySnapshot = await getDocs(query(busStopRef, where("BUSS_SENSE", "==", busStopName)));

        const busStopsList = [];
        querySnapshot.forEach((doc) => {
            const busStopData = doc.data();
            const cleanedBusStop = {
                id: doc.id,
                data: {
                    BUSL_ID: busStopData.BUSL_ID.trim(),
                    BUSS_ID: busStopData.BUSS_ID.trim(),
                    BUSS_MAINSTREET: busStopData.BUSS_MAINSTREET.trim(),
                    BUSS_NAME: busStopData.BUSS_NAME.trim(),
                    BUSS_NODE: busStopData.BUSS_NODE,
                    BUSS_SECONDARYSTREET: busStopData.BUSS_SECONDARYSTREET.trim(),
                    BUSS_SENSE: busStopData.BUSS_SENSE.trim(),
                },
            };
        busStopsList.push(cleanedBusStop);
        });

      // Ordenar el array busStopsList por BUSS_NODE de menor a mayor
        busStopsList.sort((a, b) => a.data.BUSS_NODE - b.data.BUSS_NODE);

        response.status(200).json(busStopsList);

    } catch (error) {
        response.status(500).json({ message: error.message });
    }
}; 

export const editBusStop = async (request, response) => {
    try {
        const busStopId = request.params.id;
        const busStopsRef = collection(db, "busStops");
        const querySnapshot = await getDocs(query(busStopsRef, where("BUSS_ID", "==", busStopId)));

        if (querySnapshot.empty) {
            return response.status(404).send("Bus Stop doesn't exist");
        }

        const busStopDoc = querySnapshot.docs[0];
        const busStopRef = doc(db, "busStops", busStopDoc.id);

        const { BUSL_ID, BUSS_ID, BUSS_MAINSTREET, BUSS_NAME,BUSS_NODE, BUSS_SECONDARYSTREET, BUSS_SENSE } = request.body;

        await updateDoc(busStopRef, {
            BUSL_ID,BUSS_ID, BUSS_MAINSTREET, BUSS_NAME,BUSS_NODE, BUSS_SECONDARYSTREET, BUSS_SENSE
        });

        const updatedBusStopDoc = await getDoc(busStopRef);
        const updateBusStop = {
            BUSL_ID: updatedBusStopDoc.BUSL_ID.trim(),
            BUSS_ID: updatedBusStopDoc.BUSS_ID.trim(),
            BUSS_MAINSTREET: updatedBusStopDoc.BUSS_MAINSTREET.trim(),
            BUSS_NAME: updatedBusStopDoc.BUSS_NAME.trim(),
            BUSS_NODE: updatedBusStopDoc.BUSS_NODE.trim(),
            BUSS_SECONDARYSTREET: updatedBusStopDoc.BUSS_SECONDARYSTREET.trim(),
            BUSS_SENSE: updatedBusStopDoc.BUSS_SENSE.trim(),
        };

        response.status(200).json(updateBusStop);
    } catch (error) {
        response.status(500).json({ message: error.message });
    }
};

export const deleteBusStop = async (request, response) => {
    try {
        const busStopId = request.params.id;
        const busStopsRef = collection(db, "busStops");
        const querySnapshot = await getDocs(query(busStopsRef, where("BUSS_ID", "==", busStopId)));

        if (querySnapshot.empty) {
            return response.status(404).send("Bus Stop doesn't exist");
        }

        const busStopDoc = querySnapshot.docs[0];
        const busStopRef = doc(db, "busStops", busStopDoc.id);

        await deleteDoc(busStopRef);

        response.status(200).send("Bus Stop deleted successfully");
    } catch (error) {
        response.status(500).json({ message: error.message });
    }
};