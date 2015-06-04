
import jopencc.Convertor;

public class ZhtZhsConvertor extends Convertor{
	public ZhtZhsConvertor(String jopenccPath){
		super(jopenccPath);
	}
	public String toZht(String zhs){
		return convertToZht(zhs);
	}
	public static String[] toZht(String[] zhs){
		if(zhs == null){ return null; }
		String[] buf = String[zhs.length];
		for(int i = 0; i < zhs.length; i++){
			buf[i] = convertToZht(zhs[i]);
		}
		return buf;
	}
	public static String toZhs(String zht){
		return convertToZhs(zht);
	}
	public static String[] toZhs(String zht){
		if(zht == null){ return null; }
		String[] buf = String[zht.length];
		for(int i = 0; i < zht.length; i++){
			buf[i] = convertToZhs(zht[i]);
		}
		return buf;
	}
}