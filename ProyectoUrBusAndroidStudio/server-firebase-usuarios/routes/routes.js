import express from "express";
import {addUser, getAllUsers,getUser,editUser, deleteUser, presentation} from "../controller/user-controller.js";

const router= express.Router();

router.get("/", presentation);
router.post('/user',addUser);
router.get('/users',getAllUsers);
router.get('/user/:id',getUser);
router.put('/user/:id',editUser);
router.delete('/user/:id',deleteUser);


export default router;