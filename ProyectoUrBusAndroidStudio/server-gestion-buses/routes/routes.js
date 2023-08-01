import express from "express";
import {addBusStop, getAllBusStops ,getBusStop,editBusStop, deleteBusStop, presentation} from "../controller/bus-stop-controller.js";

const router= express.Router();

router.get("/", presentation);
router.post('/buss',addBusStop);
router.get('/buss',getAllBusStops);
router.get('/buss/:id',getBusStop);
router.put('/buss/:id',editBusStop);
router.delete('/buss/:id',deleteBusStop);


export default router;