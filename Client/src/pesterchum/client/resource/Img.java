package pesterchum.client.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Img extends ResourceSuper{
	private BufferedImage image;
	public Img(String path){
		super(path);
	}
	public Img(String name, String path){
		super(name, path);
	}
	@Override
	public boolean load(InputStream is) {
		try {
			image = ImageIO.read(is);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	public BufferedImage getImage(){
		return image;
	}
}
