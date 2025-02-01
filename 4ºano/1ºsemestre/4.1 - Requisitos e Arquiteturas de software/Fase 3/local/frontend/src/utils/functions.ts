import {jwtDecode} from "jwt-decode";

export interface TokenPayload {
  plan: string;
  email: string; 
  name: string;
  id: string;
}

export function getPlan(): string | null {
  try {
    const token = localStorage.getItem('token');

    if (!token) {
      return null;
    }

    const decoded = jwtDecode<TokenPayload>(token);
    //console.log("Decoded token plan:", decoded);

    return decoded.plan || null;
  } catch (error) {
    console.error("Erro ao decodificar o token JWT:", error);
    return null;
  }
}

export function getUser(){
    try {
        const token = localStorage.getItem('token');
    
        if (!token) {
          return null;
        }
    
        const decoded = jwtDecode<TokenPayload>(token);
        //console.log("Decoded token:", decoded);
    
        return decoded.email || null;
      } catch (error) {
        console.error("Erro ao decodificar o token JWT:", error);
        return null;
      }
}

export function getName(){
    try {
        const token = localStorage.getItem('token');
    
        if (!token) {
          return null;
        }
    
        const decoded = jwtDecode<TokenPayload>(token);
        //console.log("Decoded token:", decoded);
    
        return decoded.name || null;
      } catch (error) {
        console.error("Erro ao decodificar o token JWT:", error);
        return null;
      }
}

export function getId(){
    try {
        const token = localStorage.getItem('token');
    
        if (!token) {
          return null;
        }
    
        const decoded = jwtDecode<TokenPayload>(token);
        //console.log("Decoded token:", decoded);
    
        return decoded.id || null;
      } catch (error) {
        console.error("Erro ao decodificar o token JWT:", error);
        return null;
      }
}
