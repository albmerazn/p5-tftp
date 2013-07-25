package org.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestServidor {

	/**
	 * @param args
	 */
public static void main(String[] args) throws IOException {
		
		File archTrace=new File("traceServidor.txt");		
		FileOutputStream fos=new FileOutputStream(archTrace);
		String mensaje;
		String ruta=args[0];
		int timeout=1000;
		int intentos=0;
		boolean trace=false,verbose=false;
		
		int argumentos=args.length;
		if(argumentos==2)
		{
			if(args[0].compareTo("-t")==0)
				trace=true;
			if(args[0].compareTo("-v")==0)
				verbose=true;
			ruta=args[1];
		}
		if(argumentos==3)
		{
			if(args[0].compareTo("-t")==0||args[1].compareTo("-t")==0)
				trace=true;
			if(args[0].compareTo("-v")==0||args[1].compareTo("-v")==0)
				verbose=true;

			ruta=args[2];
		}
		if(argumentos==4)
		{
			if(args[0].compareTo("-t")==0||args[1].compareTo("-t")==0||args[2].compareTo("-t")==0)
				trace=true;
			if(args[0].compareTo("-v")==0||args[1].compareTo("-v")==0||args[2].compareTo("-v")==0)
				verbose=true;

			ruta=args[3];
		}
		

		TFTP servidor=new TFTP(69,ruta);
		
		mensaje="\nServidor TFTP lanzado en el puerto 69\n";
		if(trace)
			fos.write(mensaje.getBytes());					
		if(verbose)
			System.out.println("SERVIDOR:\t"+mensaje);
		
		String archivo="";
		
		while(true){
			byte[] paquete=new byte[518];
			paquete=servidor.recibirPaqueteServidor(paquete);
			switch(servidor.catalogarPaquete(paquete)){
				case 1:	//RRQ					
					archivo=servidor.desempaquetarRRQyWRQ(paquete);
					
					mensaje="Petición de lectura recibida.\n";
					if(trace)
						fos.write(mensaje.getBytes());					
					if(verbose)				
						System.out.println("SERVIDOR:\t"+mensaje);
					
					if(servidor.cargarArchivo(archivo)){	//Carga el archivo que se ha pedido
						int bloques=servidor.numeroBloquesArchivo();	//Calcula el número de bloques que tiene
						System.out.println("Numero de bloques a enviar: "+bloques);
						byte[] paqueteRecibido=new byte[518];
						boolean bienEnviado=true;
						paquete=new byte[1];
						for(int i=0;i<bloques;i++){		//Para hacer este for y emitirlos.
							
							if(bienEnviado){
								paquete=servidor.crearPaqueteData(i+1, servidor.leerBytes(i));	//Crea un paquete.
							}
							//System.out.println("Tamaño del paquete a enviar: "+paquete.length);
							servidor.enviarPaqueteServidor(paquete);	//Lo envia al cliente.
							
							mensaje="["+(i+1)+"] Data enviado\n";
							if(trace)
								fos.write(mensaje.getBytes());					
							if(verbose)					
								System.out.println("SERVIDOR:\t"+mensaje);
							
							paqueteRecibido=servidor.recibirPaquete(paqueteRecibido, timeout);	//Espera la respuesta
							//System.out.println("longitud de paquete Recibido: "+paqueteRecibido.length);
							if(paqueteRecibido!=null){
								if(servidor.catalogarPaquete(paqueteRecibido)==4){	//Comprueba que es un ACK.
									int ack=servidor.desempaquetarACK(paqueteRecibido);	//Desempaqueta el ACK	
									if(ack!=(i+1)){	//Comprueba que no sea un ACK diferente, si lo es, se retransmite el último
										bienEnviado=false;
										i--;
									}else{
										//System.out.println("Enviado correctamente el bloque: "+(i+1));
										bienEnviado=true;
										paquete=new byte[1];
									
										mensaje="["+ack+"] ACK recibido\n";
										if(trace)
											fos.write(mensaje.getBytes());					
										if(verbose)					
											System.out.println("SERVIDOR:\t"+mensaje);
									}
								}else{ 
									if(servidor.catalogarPaquete(paqueteRecibido)==5)//Comprueba que es un error
									{
										mensaje=servidor.desempaquetarError(paqueteRecibido);
										if(trace)
											fos.write(mensaje.getBytes());					
										if(verbose)					
											System.out.println("SERVIDOR:\t"+mensaje);
									}
								}
							}else{
								intentos++;
								mensaje="No se ha recibido respuesta del cliente";
								if(trace)
									fos.write(mensaje.getBytes());					
								if(verbose)					
									System.out.println("SERVIDOR:\t"+mensaje);
								if(intentos<3){
									bienEnviado=false;
									i--;
								}else{
									mensaje="Se ha perdido la conexión con el cliente.";
									if(trace)
										fos.write(mensaje.getBytes());					
									if(verbose)					
										System.out.println("SERVIDOR:\t"+mensaje);
									break;
								}
							}
						}
						servidor.terminarLectura();
					}else{	//El archivo no existe.
						//Enviar error
						paquete=servidor.crearPaqueteError(1, "");
						servidor.enviarPaqueteServidor(paquete);
						
						mensaje="Archivo no encontrado\n";
						if(trace)
							fos.write(mensaje.getBytes());					
						if(verbose)					
							System.out.println("SERVIDOR:\t"+mensaje);
					}
					
					mensaje="Archivo enviado completamente";
					if(trace)
						fos.write(mensaje.getBytes());					
					if(verbose)					
						System.out.println("SERVIDOR:\t"+mensaje);
					servidor.terminarLectura();
					break;
				case 2:	//WRQ
					mensaje="Petición de escritura recibida.\n";
					if(trace)
						fos.write(mensaje.getBytes());					
					if(verbose)					
						System.out.println("SERVIDOR:\t"+mensaje);
					
					archivo=servidor.desempaquetarRRQyWRQ(paquete);
					if(servidor.crearArchivo(archivo)){	//Carga el archivo que se ha pedido
						//Enviamos ack 0.
						servidor.enviarPaqueteServidor(servidor.crearPaqueteACK(0));
						
						mensaje="[0] ACK enviado\n";
						if(trace)
							fos.write(mensaje.getBytes());					
						if(verbose)					
							System.out.println("SERVIDOR:\t"+mensaje);
						
						int tamano=0;
						int bloque=0;
						byte[] respuesta=new byte[4];
						do{
							byte[] datos=new byte[1];
							paquete=servidor.recibirPaquete(paquete, timeout);
							if(paquete!=null){
								if(servidor.catalogarPaquete(paquete)==3){
									bloque=servidor.numeroPaqueteDatos(paquete);
									datos=servidor.desempaquetarDatos(paquete);
									tamano=datos.length;								
								
									mensaje="["+bloque+"] Data recibido\n";
									if(trace)
										fos.write(mensaje.getBytes());					
									if(verbose)					
										System.out.println("SERVIDOR:\t"+mensaje);
								
									if(servidor.comprobarCRC(paquete, datos)){
										servidor.escribirBytes(datos, bloque);
										respuesta=servidor.crearPaqueteACK(bloque);
										servidor.enviarPaqueteServidor(respuesta);
									
										mensaje="["+bloque+"] ACK enviado\n";
										if(trace)
											fos.write(mensaje.getBytes());					
										if(verbose)					
											System.out.println("SERVIDOR:\t"+mensaje);
									}else{
										respuesta=servidor.crearPaqueteACK(bloque-1);
										servidor.enviarPaqueteServidor(respuesta);
										mensaje="["+(bloque-1)+"] Data recibido\n";
										if(trace)
											fos.write(mensaje.getBytes());					
										if(verbose)					
											System.out.println("SERVIDOR:\t"+mensaje);
									}
								}else{
									mensaje=servidor.desempaquetarError(paquete);
									if(trace)
										fos.write(mensaje.getBytes());					
									if(verbose)					
										System.out.println("SERVIDOR:\t"+mensaje);
									tamano=0;
								}
							}else{ //Aquí hay que mirar si no ha llegado un ack.
								intentos++;
								mensaje="No se ha recibido respuesta al ACK "+bloque;
								if(trace)
									fos.write(mensaje.getBytes());					
								if(verbose)					
									System.out.println("SERVIDOR:\t"+mensaje);
								if(intentos<3){
									servidor.enviarPaqueteServidor(servidor.crearPaqueteACK(bloque));	//Se vuelve a enviar el ack
								}else{
									mensaje="Se ha perdido la conexión con el cliente";
									if(trace)
										fos.write(mensaje.getBytes());					
									if(verbose)					
										System.out.println("SERVIDOR:\t"+mensaje);
									break;
								}
							}
							
						}while(tamano>=512);
						servidor.terminarEscritura();
					}else{	//El archivo ya existe.
						//Enviar error
						paquete=servidor.crearPaqueteError(6, "");
						servidor.enviarPaqueteServidor(paquete);
						
						mensaje="El archivo ya existe";
						if(trace)
							fos.write(mensaje.getBytes());					
						if(verbose)					
							System.out.println("SERVIDOR:\t"+mensaje);
					}
					break;
				default:	//ERROR
					paquete=servidor.crearPaqueteError(4, "");
					servidor.enviarPaqueteServidor(paquete);
					mensaje="Operacion TFTP ilegal";
					if(trace)
						fos.write(mensaje.getBytes());					
					if(verbose)					
						System.out.println("SERVIDOR:\t"+mensaje);
					break;
			}
			fos.close();
		}
	}

}
