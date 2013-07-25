package org.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class TestCliente {
	
	public static void main(String[] args) throws IOException
	{
		TFTP conexion=new TFTP();
		String hostServer=args[0];
		
		File archTrace=new File("traceCliente.txt");
		
		FileOutputStream fos=new FileOutputStream(archTrace);
		boolean trace = false;
		boolean verbose = false;
		boolean perdida = false;
		String mensaje;
		
		int opcion=0;
		String nombreArchivo;		
		String cadenaLeida;
		byte[] paquete;
		int porcentaje;
		int argumentos=args.length;
		int intentos=0;
		int timeout=1000;
		
		if(argumentos==2)
		{
			if(args[0].compareTo("-t")==0)
				trace=true;
			if(args[0].compareTo("-v")==0)
				verbose=true;
			if(args[0].contains("-d"))
			{
				perdida=true;
				char aux[]=new char[args[1].length()-2];
				for(int i=0;i<args[0].length();i++)
					aux[i]=args[0].charAt(i+2);
				
				porcentaje=Integer.parseInt(aux.toString());
			}
			hostServer=args[1];
		}
		if(argumentos==3)
		{
			if(args[0].compareTo("-t")==0||args[1].compareTo("-t")==0)
				trace=true;
			if(args[0].compareTo("-v")==0||args[1].compareTo("-v")==0)
				verbose=true;
			
			if(args[0].contains("-d"))
			{
				perdida=true;
				char aux[]=new char[args[1].length()-2];
				for(int i=0;i<args[0].length();i++)
					aux[i]=args[0].charAt(i+2);
				porcentaje=Integer.parseInt(aux.toString());
			}
			if(args[1].contains("-d"))
			{
				perdida=true;
				char aux[]=new char[args[1].length()-2];
				for(int i=0;i<args[1].length();i++)
					aux[i]=args[1].charAt(i+2);
				porcentaje=Integer.parseInt(aux.toString());
			}			
			hostServer=args[2];
		}
		if(argumentos==4)
		{
			if(args[0].compareTo("-t")==0||args[1].compareTo("-t")==0||args[2].compareTo("-t")==0)
				trace=true;
			if(args[0].compareTo("-v")==0||args[1].compareTo("-v")==0||args[2].compareTo("-v")==0)
				verbose=true;
			for(int i=0;i<4;i++)
			{
				if(args[i].contains("-d"))
				{
					perdida=true;
					char aux[]=new char[args[1].length()-2];
					for(int x=0;x<args[0].length();x++)
						aux[x]=args[0].charAt(x+2);
					porcentaje=Integer.parseInt(aux.toString());
				}
			}
			hostServer=args[3];
		}
		
		
        System.out.println("Ejecución del cliente:\n");
        System.out.println("Escriba \"salir\" para terminar la aplicacion.\n");
        System.out.print("tftp> ");
        Scanner sc = new Scanner(System.in);
        cadenaLeida = sc.nextLine();
        
        if(cadenaLeida.compareToIgnoreCase("Salir")==0){
        	mensaje="\nFin del programa\n";
    		if(trace) 
    			fos.write(mensaje.getBytes());								
    		if(verbose)
    			System.out.println("CLIENTE:\t"+mensaje);	
        	return;
        }
        if(cadenaLeida.compareTo("get")==0)
        {
        	opcion = 1;
    		if(trace) 
    		{
    			mensaje="\nOperación GET solicitada\n";
    			fos.write(mensaje.getBytes());
    		}
    		if(verbose)
    			System.out.println("Operacion GET solicitada");
        }
        else
        {
        	if(cadenaLeida.compareTo("put")==0)
        	{
        		opcion = 2;
        		if(trace) 
        		{
        			mensaje="\nOperación PUT solicitada\n";
        			fos.write(mensaje.getBytes());
        		}
        		if(verbose)
        			System.out.println("Operacion PUT solicitada");
        	}
        	else
        	{
        		mensaje="Comando no válido.\n";
        		System.out.println(mensaje+" Escriba put para enviar un archivo o get para recibirlo");
        		if(trace) 
        			fos.write(mensaje.getBytes());
        		if(verbose)
        			System.out.println("CLIENTE:\t"+mensaje);
        	}
        }

		if(opcion!=0)
		{
			System.out.println("Introduzca el nombre del archivo");
			cadenaLeida = sc.nextLine();
			nombreArchivo = cadenaLeida;
			switch(opcion){
				case 1:	//RRQ
					if(trace) 
					{
						mensaje="Se intenta recibir el archivo "+ nombreArchivo +"\n";
						fos.write(mensaje.getBytes());
					}
					if(verbose)
						System.out.println("CLIENTE:\tSe intenta recibir el archivo "+ nombreArchivo);
					
					paquete=conexion.crearPaqueteRRQoWRQ(nombreArchivo, 1);
					conexion.crearArchivo(nombreArchivo);
EnviarRRQ:			if(conexion.enviarPaquete(hostServer, 69, paquete)){	//Aquí se envia el paquete de RRQ.
						intentos=0;
						int tamano=0;
						int bloque=0;
						boolean bienRecibido=true;
						paquete=new byte[518];
						byte[] respuesta=new byte[4];
						do{
							byte[] datos=new byte[1];
							paquete=conexion.recibirPaquete(paquete, timeout);	//Aquí se recibe el paquete a analizar si hace timeout puede ser que el servidor no responda o se haya perdido un paquete.
							if(paquete!=null){
								intentos=0;
								if(conexion.catalogarPaquete(paquete)==3){
									bloque=conexion.numeroPaqueteDatos(paquete);
									datos=conexion.desempaquetarDatos(paquete);
									tamano=datos.length;
									bienRecibido=conexion.comprobarCRC(paquete, datos);
									if(bienRecibido){
										if(trace) 
										{
											mensaje="["+bloque+"] Data recibido\n";
											fos.write(mensaje.getBytes());
										}
										if(verbose){
											System.out.println("CLIENTE:\t"+"["+bloque+"] Data recibido");
										}
								
										conexion.escribirBytes(datos, bloque);
										respuesta=conexion.crearPaqueteACK(bloque);
										conexion.enviarPaquete(hostServer, 69, respuesta);
									}else{
										if(trace) 
										{
											mensaje="["+bloque+"] Data recibido de forma corrupta\n";
											fos.write(mensaje.getBytes());
										}
										if(verbose){
											System.out.println("CLIENTE:\t"+"["+bloque+"] Data recibido de forma corrupta\n");
										}
										respuesta=conexion.crearPaqueteACK(bloque-1);
										conexion.enviarPaquete(hostServer, 69, respuesta);									
									}
								
									if(trace) 
									{
										mensaje="["+bloque+"] ACK enviado.\n";
										fos.write(mensaje.getBytes());
									}
									if(verbose)
										System.out.println("CLIENTE:\t"+"["+bloque+"] ACK enviado.");
									
								
								}else{
									mensaje=conexion.desempaquetarError(paquete);
									System.out.println(mensaje);
									tamano=0;
								
									if(trace) 
										fos.write(mensaje.getBytes());								
									if(verbose)
										System.out.println("CLIENTE:\t"+mensaje);								
								}
							}else{
								intentos++;
								if(bloque==0){
									mensaje="El servidor no responde, intento "+intentos;
								}else{
									mensaje="Se ha perdido el paquete "+(bloque+1);
								}
								if(trace) 
									fos.write(mensaje.getBytes());								
								if(verbose)
									System.out.println("CLIENTE:\t"+mensaje);		
								if(intentos<3){	//si no se ha llegado al tope de intentos.
									if(bloque==0){
										break EnviarRRQ;
									}else{
										conexion.enviarPaquete(hostServer, 69, respuesta);	
									}
								}else{
									mensaje="No se puede alcanzar el servidor, compruebe la conexión de red";
									System.out.println(mensaje);
									if(trace) 
										fos.write(mensaje.getBytes());	
									return;
								}
							}
						}while(tamano>=512);
						conexion.terminarEscritura();
					}else{
						//No se ha podido enviar el paquete.
						intentos++;
						mensaje="No se ha podido enviar el mensaje, intento "+intentos;
						if(trace) 
							fos.write(mensaje.getBytes(mensaje));								
						if(verbose)
							System.out.println(mensaje);
						if(intentos<3){
							break EnviarRRQ;
						}
						
					}
					break;
				case 2:	//WRQ
					
					if(trace) 
					{
						mensaje="Se intenta transferir el archivo "+ nombreArchivo +"\n";
						fos.write(mensaje.getBytes());
					}
					if(verbose)
						System.out.println("CLIENTE:\t"+"Se intenta transferir el archivo "+ nombreArchivo);
					
					int bloqueActual=-1;
					if(conexion.cargarArchivo(nombreArchivo)){	//Carga el archivo que se ha pedido
						paquete=conexion.crearPaqueteRRQoWRQ(nombreArchivo, 2);
EnviarWRQ:				if(conexion.enviarPaquete(hostServer, 69, paquete)){
							if(trace) 
							{
								mensaje="Se envía la petición  WRQ\n";
								fos.write(mensaje.getBytes());
							}
							if(verbose)
								System.out.println("CLIENTE:\t"+"Se envía la petición WRQ");
	
							paquete=conexion.recibirPaquete(paquete, timeout);
							if(paquete!=null){
								intentos=0;
								if(conexion.catalogarPaquete(paquete)==4&&conexion.desempaquetarACK(paquete)==0){
									//Empieza la emisión
									int bloques=conexion.numeroBloquesArchivo();	//Calcula el número de bloques que tiene

									if(trace) 
									{
										mensaje="Número de bloques a enviar "+bloques+"\n";
										fos.write(mensaje.getBytes());
									}
									if(verbose)
										System.out.println("CLIENTE:\t"+"Número de bloques a enviar "+bloques);
									
								
									System.out.println("Numero de bloques a enviar: "+bloques);
									byte[] paqueteRecibido=new byte[518];
									boolean bienEnviado=true;
									for(int i=0;i<bloques;i++){		//Para hacer este for y emitirlos.
										bloqueActual=i;										
										if(bienEnviado){
											paquete=new byte[1];
											paquete=conexion.crearPaqueteData(i+1, conexion.leerBytes(i));	//Crea un paquete.
										}
										
										conexion.enviarPaquete(hostServer, 69,paquete);	//Lo envia al cliente.
										
										if(trace) 
										{
											mensaje="["+(i+1)+"] Data enviado\n";
											fos.write(mensaje.getBytes());
										}
										if(verbose)
											System.out.println("CLIENTE:\t"+"["+(i+1)+"] Data enviado");
										
										paqueteRecibido=conexion.recibirPaquete(paqueteRecibido, timeout);	//Espera la respuesta
										intentos=0;
										if(paqueteRecibido!=null){
											if(conexion.catalogarPaquete(paqueteRecibido)==4){	//Comprueba que es un ACK.
												int ack=conexion.desempaquetarACK(paqueteRecibido);	//Desempaqueta el ACK	
												if(ack!=(i+1)){	//Comprueba que no sea un ACK diferente, si lo es, se retransmite el último
													bienEnviado=false;
													i--;
												}else{
													bienEnviado=true;													
													if(trace) 
													{
														mensaje="["+ack+"] ACK recibido\n";
														fos.write(mensaje.getBytes());
													}
													if(verbose)
														System.out.println("CLIENTE:\t"+"["+ack+"] ACK recibido");
												}
											}else{
												if(conexion.catalogarPaquete(paqueteRecibido)==5){
													mensaje=conexion.desempaquetarError(paqueteRecibido);
													if(trace) 
														fos.write(mensaje.getBytes());
													if(verbose)
														System.out.println("CLIENTE:\t"+mensaje);
												
												}
											}
										}else{	//No llega respuesta
											intentos++;
											if(intentos<3){
												bienEnviado=false;
												mensaje="No se ha podido enviar el paquete de datos, intento"+intentos;
												if(trace) 
													fos.write(mensaje.getBytes());
												if(verbose)
													System.out.println("CLIENTE:\t"+mensaje);
											}else{
												mensaje="Se ha perdido la conexión con el servidor, compruebe que está conectado a la red\n";
												if(trace) 
												{
													mensaje="Se ha perdido la conexión con el servidor, compruebe que está conectado a la red\n";
													fos.write(mensaje.getBytes());
												}
												System.out.println(mensaje);
												return;
											}
										}
									}
								}else{
									if(trace) 
									{
										mensaje="Error en el servidor porque no ha podido crear el archivo.\n";
										fos.write(mensaje.getBytes());
									}
									if(verbose)
										System.out.println("Error en el servidor porque no ha podido crear el archivo.");
								}
							}else{	//Se ha hecho timeout
								if(intentos<3){
									//Se ha enviado la petición pero no ha llegado
									mensaje="No se ha podido alcanzar el servidor.";
									if(trace) 
										fos.write(mensaje.getBytes());
									if(verbose)
										System.out.println("CLIENTE:\t"+mensaje);
									break EnviarWRQ;
								}else{
									mensaje="No se puede alcanzar el servidor, compruebe la conexión de red";
									if(trace) 
										fos.write(mensaje.getBytes());
								}
							}
						}else{
							if(trace) 
							{
								mensaje="No se ha podido enviar el paquete.\n";
								fos.write(mensaje.getBytes());
							}
							if(verbose)
								System.out.println("No se ha podido enviar el paquete.");
						}
					}
					break;
				default:
					if(trace) 
					{
						mensaje="Comando incorrecto.\n";
						fos.write(mensaje.getBytes());
					}
					if(verbose)
						System.out.println("Comando incorrecto.");
					break;
			}
			
		}
		mensaje="\nFin de la transmisión";
		if(trace) 
			fos.write(mensaje.getBytes());		
		if(verbose)
			System.out.println(mensaje);	
		
		System.out.println("Fin del programa");
		
		fos.close();
	}
}