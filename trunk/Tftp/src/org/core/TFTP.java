package org.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
	private String ruta;
	private File archivo;
	private FileInputStream fis;
	private FileOutputStream fos;
	private int tamanoFichero;
	
	//Esto solo para el servidor:
	private InetAddress direccion;
	private int puerto;
	private static final int poly = 0x8005;/* x16 + x15 + x2 + 1 polinomio generador */
	private static final int[] crcTable = new int[ 256 ];
	
	/**
	 * Constructor por defecto, crea un socket donde se pueda.
	 */
	public TFTP(){
		try {
			ruta=new java.io.File(".").getCanonicalPath()+"//";
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		tamanoFichero=0;
		crcActivo=true;
		calcularTablaCRC();
		try {
			socket=new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	/**
	 * Constructor para indicar el puerto del socket a crear.
	 * @param puerto
	 */
	public TFTP(int puerto, String ruta){
		tamanoFichero=0;
		crcActivo=true;
		calcularTablaCRC();
		this.ruta=ruta;
		try {
			socket=new DatagramSocket(puerto);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	/**
	 * Cierra el socket
	 */
	public void cerrarConexion(){
		socket.close();
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
	
	public boolean enviarPaqueteServidor(byte[] paquete){
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
	
	public byte[] recibirPaquete(byte[] datos, int timeout){
		DatagramPacket paquete = new DatagramPacket(datos, datos.length);
        try {
        	if(timeout!=0){
        		socket.setSoTimeout(timeout);	//Esto es el timeout.
        	}
			socket.receive(paquete);
			byte[] aux=new byte[paquete.getLength()];
			byte[] aux2=paquete.getData();
			for(int i=0;i<aux.length;i++){
				aux[i]=aux2[i];
			}
			//System.out.println(paquete.getLength()+"<-Paquete Array->"+aux.length);
			return aux;
			//return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return null;
	}
	
	public byte[] recibirPaqueteServidor(byte[] datos){
		DatagramPacket paquete = new DatagramPacket(datos, datos.length);
        try {
        	//socket.setSoTimeout(timeout);	//Esto es el timeout.
        	socket.setSoTimeout(0);
			socket.receive(paquete);
			direccion=paquete.getAddress();
			puerto=paquete.getPort();
			return paquete.getData();
			//return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
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
		
		if(data!=null){
			if(crcActivo){
				paquete=new byte[data.length+6];
			}else{
				paquete=new byte[data.length+4];
			}
		}else{
			if(crcActivo){
				paquete=new byte[6];
			}else{
				paquete=new byte[4];
			}
		}
		
		paquete[0]=c[0];
		paquete[1]=c[1];
		paquete[2]=b[0];
		paquete[3]=b[1];
		if(data!=null){
			for(int i=0;i<data.length;i++){
				paquete[i+4]=data[i];
			}
		}
		if(crcActivo){
			if(data!=null){
				byte[] crc=intAByte(calcularCRC(data));
				paquete[paquete.length-2]=crc[0];
				paquete[paquete.length-1]=crc[1];
			}else{
				paquete[paquete.length-1]=zero;
				paquete[paquete.length-2]=zero;
			}
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
	
	/**
	 * Carga un archivo para ser transmitido.
	 * @param fichero Nombre del archivo a abrir.
	 * @return True si se ha podido abrir el archivo.
	 */
	public boolean cargarArchivo(String fichero){
		archivo=new File(ruta+fichero);
		if(!archivo.exists()){
			return false;
		}
		tamanoFichero=(int) archivo.length();
		try {
			fis=new FileInputStream(archivo);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}	
		
		return true;
	}
	
	/**
	 * Devuelve el número de bloques que hay.
	 * @return
	 */
	public int numeroBloquesArchivo(){
		return (tamanoFichero/512)+1;
	}
	
	/**
	 * Método que lee una cantidad de bytes concreta del archivo cargado.
	 * @param offset
	 * @return
	 */
	public byte[] leerBytes( int bloque){
		int pos=bloque*512;
		int longitud=512;
		if(tamanoFichero-pos>0){
			if(tamanoFichero-pos<512){
				longitud=tamanoFichero-pos;
			}
			byte[] aux=new byte[longitud];
			try {
				//fis.read(aux, pos, longitud);
				fis.read(aux);
				return aux;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Método para escribir datos en el archivo creado anteriormente.
	 * @param b	Bytes recibidos para escribir.
	 * @param bloque Bloque que nos dirá donde se escriben.
	 * @return True si se han escrito.
	 */
	public boolean escribirBytes(byte[] b, int bloque){
		try {
			//fos.write(b, bloque*512, b.length);
			fos.write(b);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Metodo para crear un archivo.
	 * @param fichero
	 * @return Devuelve false si no se ha podido crear el archivo.
	 */
	public boolean crearArchivo(String fichero){
		File archivo=new File(ruta+fichero);
		if(archivo.exists()){
			return false;
		}
		try {
			archivo.createNewFile();
			fos=new FileOutputStream(archivo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean terminarLectura(){
		try {
			fis.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Cierra el archivo para terminar la escritura.
	 * @return 
	 */
	public boolean terminarEscritura(){
		try {
			fos.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Calcula la tabla CRC para acelerar los cálculos.
	 */
	private void calcularTablaCRC(){
		// initialise scrambler table
        for ( int i = 0; i < 256; i++ ){
            int fcs = 0;
            int d = i << 8;
            for ( int k = 0; k < 8; k++ )
                {
                if ( ( ( fcs ^ d ) & 0x8000 ) != 0 )
                    {
                    fcs = ( fcs << 1 ) ^ poly;
                    }
                else
                    {
                    fcs = ( fcs << 1 );
                    }
                d <<= 1;
                fcs &= 0xffff;
                }
            crcTable[ i ] = fcs;
        }
	}
	
	/**
	 * Método para calcular el crc de un paquete de datos.
	 * @param datos Paquete al que se le quiere calcular el crc.
	 * @return	Entero con el resultado.
	 */
	private int calcularCRC(byte[] datos){
		// loop, calculating CRC for each byte of the string
        int work = 0xffff;
        for ( byte b : datos )
            {
            // xor the next data byte with the high byte of what we have so far to
            // look up the scrambler.
            // xor that with the low byte of what we have so far.
            // Mask back to 16 bits.
            work = ( crcTable[ ( b ^ ( work >>> 8 ) ) & 0xff ] ^ ( work << 8 ) ) &
                   0xffff;
            }
        return work;
	}
	
	/**
	 * Comprueba que el CRC del paquete sea correcto.
	 * @param paquete
	 * @return
	 */
	public boolean comprobarCRC(byte[] paquete, byte[] datos){
		byte[] aux=new byte[2];
		aux[0]=paquete[paquete.length-2];
		aux[1]=paquete[paquete.length-1];
		int crc1=bytesAint(aux);
		int crc2=calcularCRC(datos);
		if(crc1-crc2==0){
			return true;
		}
		return false;
	}
}
