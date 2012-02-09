package org.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TFTP {
	
	//Atributos necesarios.
	private DatagramSocket socket;
	
	private static final byte zero=0;	
	private boolean crcActivo;
	
	/**
	 * Constructor por defecto.
	 */
	public TFTP(){
		crcActivo=false;
		try {
			socket=new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	public boolean enviarPaquete(String host, int puerto, byte[] paquete){
		InetAddress direccion;
		try {
			direccion = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		DatagramPacket datos = new DatagramPacket(paquete, paquete.length, direccion, puerto);
        
        try {
			socket.send(datos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public byte[] recibirPaquete(byte[] datos){
		DatagramPacket paquete = new DatagramPacket(datos, datos.length);
		try {
			//socket.setSoTimeout(timeout);	//Esto es el timeout.
			socket.receive(paquete);
			return paquete.getData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * Lee el código de operación del paquete para saber como tratarlo.
	 * @param paquete Paquete que se desea identificar.
	 * @return	Número entero indicando el tipo de paquete que es.
	 */
	public int catalogarPaquete(byte[] paquete){
		byte[] codigo=new byte[2];
		codigo[0]=paquete[0];
		codigo[1]=paquete[1];
		int c=bytesAint(codigo);
		return c;	//Devuelve 1 para RRQ, 2 para WRQ, 3 para Data, 4 ACK, 5 para error.
	}
	
	/**
	 * Extrae de un paquete el nombre del fichero a leer o escribir.
	 * @param paquete	Paquete recibido a examinar.
	 * @return	String con el nombre del fichero.
	 */
	public String desempaquetarRRQyWRQ(byte[] paquete){
		byte[] aux=new byte[paquete.length-3];
		for(int i=0;i<aux.length;i++){
			aux[i]=paquete[i+2];
		}		
		return new String(aux);
	}
	
	/**
	 * Devuelve el número de paquete de datos.
	 * @param paquete Array de bytes que representa el paquete.
	 * @return Número de paquete.
	 */
	public int numeroPaqueteDatos(byte[] paquete){
		byte[] b=new byte[2];
		b[0]=paquete[2];
		b[1]=paquete[3];
		return bytesAint(b);
	}
	
	/**
	 * Este método nos devuelve los datos contenidos en el paquete TFTP
	 * @param paquete Paquete recibido
	 * @return	Paquete de datos.
	 */
	public byte[] desempaquetarDatos(byte[] paquete){
		byte[] datos;
		if(crcActivo){
			datos=new byte[paquete.length-6];
			for(int i=0;i<datos.length;i++){
				datos[i]=paquete[i+4];
			}
		}else{
			datos=new byte[paquete.length-4];
			for(int i=0;i<datos.length;i++){
				datos[i]=paquete[i+4];
			}
		}
		return datos;
	}
	
	public int desempaquetarACK(byte[] paquete){
		byte[] b=new byte[2];
		b[0]=paquete[2];
		b[1]=paquete[3];
		return bytesAint(b);
	}
	
	public String desempaquetarError(byte[] paquete){
		byte[] aux=new byte[2];
		aux[0]=paquete[2];
		aux[1]=paquete[3];
		int error=bytesAint(aux);
		byte[] aux2=new byte[paquete.length-5];
		
		switch(error){
			case 0:		//error especifico, mirar contenido.
				for(int i=0;i<aux2.length;i++){
					aux2[i]=paquete[i+4];
				}
				return new String(aux2);
			case 1:		//Archivo no encontrado.
				return "Archivo no encontrado";
			case 2:		//Violacion de acceso.
				return "Violacion de acceso";
			case 3:		//Disco lleno o capacidad excedida.
				return "Disco lleno o capacidad excedida";
			case 4:		//Operacion TFTP ilegal.
				return "Operacion TFTP ilegal";
			case 5:		//ID de transferencia desconocido.
				return "ID de transfeerencia desconocido";
			case 6:		//El archivo ya existe.
				return "El archivo ya existe";
			case 7:		//No es usuario.
				return "No es usuario";
			default:
				return "";
		}
	}
	
	/**
	 * Metodo que crea el paquete RRQ o WRQ dependiendo del código que le pasemos.
	 * @param archivo	String con el nombre del archivo a buscar.
	 * @param codigo	1 para RRQ y 2 para WRQ.
	 * @return	Array de byte con la cabecera tftp.
	 */
	public byte[] crearPaqueteRRQoWRQ(String archivo, int codigo){
		byte[] paquete;	//Array que representa el paquete.
		byte[] aux;		//Array que lleva los bytes del archivo que pedimos.
		byte[] c=intAByte(codigo);	//Array con el código de operacion.
		
		aux=archivo.getBytes();	//Pasamos el string a un array de byte.
		
		paquete=new byte[aux.length+3];	//Teniendo la longitud del array aux podemos calcular el tamaño del paquete.
		
		paquete[0]=c[0];
		paquete[1]=c[1];	//Copiamos el código de operación en el paquete.
		
		for(int i=0;i<aux.length;i++){	//Copiamos la info del string.
			paquete[i+2]=aux[i];
		}
		
		paquete[paquete.length-1]=zero;	//Copiamos el zero de final.
		return paquete;	//Paquete listo.
	}
	
	/**
	 * Metodo para crear los paquetes de datos en tftp
	 * @param bloque	Numero de bloque que corresponde al paquete enviado
	 * @param data	Datos a enviar.
	 * @return	El paquete listo para eniar.
	 */
	public byte[] crearPaqueteData(int bloque, byte[] data){
		byte[] paquete;
		byte[] b=intAByte(bloque);
		byte[] c=intAByte(3);
		
		if(data.length>0){
			paquete=new byte[data.length+4];
		}else{
			paquete=new byte[4];
		}
		
		paquete[0]=c[0];
		paquete[1]=c[1];
		paquete[2]=b[0];
		paquete[3]=b[1];
		
		for(int i=0;i<data.length;i++){
			paquete[i+4]=data[i];
		}
		
		return paquete;
	}
	
	public byte[] crearPaqueteACK(int bloque){
		byte[] c=intAByte(4);
		byte[] b=intAByte(bloque);
		byte[] ack=new byte[4];
		ack[0]=c[0];
		ack[1]=c[1];
		ack[2]=b[0];
		ack[3]=b[1];
		return ack;
	}
	
	public byte[] crearPaqueteError(int codigoError, String mensajeError){
		byte[] error;
		byte[] operacion=intAByte(5);
		byte[] codigo=intAByte(codigoError);
		byte[] mensaje;
		
		switch(codigoError){		//Se selecciona el error correspondiente.
			case 0:		//Error no definido.
				mensaje=mensajeError.getBytes();
				break;
			case 1:		//Archivo no encontrado.
				mensaje="Archivo no encontrado".getBytes();
				break;
			case 2:		//Violacion de acceso.
				mensaje="Violacion de acceso".getBytes();
				break;
			case 3:		//Disco lleno o capacidad excedida.
				mensaje="Disco lleno o capacidad excedida".getBytes();
				break;	
			case 4:		//Operacion TFTP ilegal.
				mensaje="Operacion TFTP ilegal".getBytes();
				break;
			case 5:		//ID de transferencia desconocido.
				mensaje="ID de transfeerencia desconocido".getBytes();
				break;
			case 6:		//El archivo ya existe.
				mensaje="El archivo ya existe".getBytes();
				break;
			case 7:		//No es usuario.
				mensaje="No es usuario".getBytes();
				break;
			default:	//Lo mismo que el 0.
				mensaje=mensajeError.getBytes();
				break;
		}
		error=new byte[mensaje.length+5];
		
		error[0]=operacion[0];	//Insertarmos el código de operación
		error[1]=operacion[1];
		
		error[2]=codigo[0];		//Insertamos el código de error.
		error[3]=codigo[1];
		
		for(int i=0;i<mensaje.length;i++){	//Insertamos el mensaje correspondiente.
			error[i+4]=mensaje[i];
		}
		error[error.length-1]=zero;
		
		return error;
	}
	
	/**
	 * Método que convierte un numero a byte para los paquetes.
	 * @param numero Numero entero a convertir.
	 * @return	Array de 2 bytes listo para usar en el tftp.
	 */
	private byte[] intAByte(int numero){
		byte[] b =new byte[] {(byte)(numero >>> 24),(byte)(numero >>> 16),(byte)(numero >>> 8),(byte)numero};
		byte[] aux=new byte[2];
		aux[0]=b[2];
		aux[1]=b[3];		
		return aux;
	}
	
	/**
	 * Metodo que convierte el código de bytes a un entero.
	 * @param arr Array de bytes con el número a convertir.
	 * @return El número que contiene.
	 */
	private int bytesAint(byte[] b) {
		 return ((b[0] & 0xFF) << 8) + (b[1] & 0xFF);
	}
	
}
