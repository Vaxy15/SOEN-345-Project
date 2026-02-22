import { useState } from "react";
import { auth } from "./firebase";
import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut,
} from "firebase/auth";

export default function App() {
  const [email, setEmail] = useState("");
  const [pass, setPass] = useState("");
  const [msg, setMsg] = useState("");

  const backend = import.meta.env.VITE_BACKEND_URL;

  async function register() {
    setMsg("");
    await createUserWithEmailAndPassword(auth, email, pass);
    setMsg("Registered & logged in.");
  }

  async function login() {
    setMsg("");
    await signInWithEmailAndPassword(auth, email, pass);
    setMsg("Logged in.");
  }

  async function logout() {
    await signOut(auth);
    setMsg("Logged out.");
  }

  async function callMe() {
    setMsg("");
    const user = auth.currentUser;
    if (!user) return setMsg("Not logged in.");
    const token = await user.getIdToken();

    const res = await fetch(`${backend}/api/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const text = await res.text();
    setMsg(`Status ${res.status}: ${text}`);
  }

  return (
    <div style={{ padding: 20, fontFamily: "sans-serif" }}>
      <h2>Auth → Backend test</h2>

      <div style={{ display: "flex", gap: 8, marginBottom: 8 }}>
        <input placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <input placeholder="password" type="password" value={pass} onChange={(e) => setPass(e.target.value)} />
      </div>

      <div style={{ display: "flex", gap: 8 }}>
        <button onClick={register}>Register</button>
        <button onClick={login}>Login</button>
        <button onClick={logout}>Logout</button>
        <button onClick={callMe}>Call /api/me</button>
      </div>

      <p style={{ marginTop: 12 }}>{msg}</p>
    </div>
  );
}