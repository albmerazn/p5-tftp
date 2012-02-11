package org.core;

public class TestServidor {

	String ruta;	
	
	private TestServidor()
	{
		TFTP tftp = new TFTP(69,ruta);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestServidor servidor = new TestServidor();

	}

}
