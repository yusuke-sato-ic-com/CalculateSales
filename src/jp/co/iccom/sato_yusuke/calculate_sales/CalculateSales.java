package jp.co.iccom.sato_yusuke.calculate_sales;

import java.io.*;
import java.util.*;;

public class CalculateSales {
	public static void main(String[] args) {
		// コマンドライン引数で、ディレクトリより支店定義ファイルを読み込む
		// ディレクトリまでのパス指定
		File branch = new File(args[0],"branch.lst"); // Fileオブジェクトの生成
		File commodity = new File(args[0],"commodity.lst");
		HashMap<String, String> branchmap = new HashMap<String, String>();
		HashMap<String, String> commoditymap = new HashMap<String, String>();

		try {
			FileReader fr1 = new FileReader(branch);
			BufferedReader br1 = new BufferedReader(fr1);
			String line1;
			while ((line1 = br1.readLine()) != null) {
				String[] branchData = line1.split(",");
			//	System.out.println(branchData[0] + branchData[1]);
				branchmap.put(branchData[0], branchData[1]);
			}
			br1.close(); //ストリームを閉じる
		//	System.out.println(map1.entrySet());
		} catch(FileNotFoundException fe)  { // 例外を受け取る
				System.out.println("支店定義ファイルは存在しません");
				return;
		} catch(IOException e)  { // 例外を受け取る
			System.out.println("予期せぬエラーが発生しました");
		}

		try {
			FileReader fr2 = new FileReader(commodity);
			BufferedReader br2 = new BufferedReader(fr2);
			String line2;
			while ((line2 = br2.readLine()) != null) {
				String[] commodityData = line2.split(",");
			//	System.out.println(commodityData[0] + commodityData[1]);
				commoditymap.put(commodityData[0], commodityData[1]);
			}
			br2.close();
		//	System.out.println(map2.entrySet());
		} catch(FileNotFoundException fe)  { // 例外を受け取る
			System.out.println("商品定義ファイルは存在しません");
			return;
		} catch(IOException e)  { // 例外を受け取る
			System.out.println("予期せぬエラーが発生しました");
		}
	//	System.out.println(branchmap.entrySet());
	//	System.out.println(commoditymap.entrySet());


		// ディレクトリ内のファイル一覧を取得
		String path = args[0];
		File dir = new File(path);
		File[] files = dir.listFiles(); // dir内のファイルを配列に格納
		ArrayList<String> salesfiles = new ArrayList<String>();

		for (int i = 0; i < files.length; i++) { // 配列内の要素の数だけループし一覧を取得
			File Infile = files[i];
			if (Infile.getName().endsWith(".rcd")) {
				try {
					FileReader frI = new FileReader(Infile);
					BufferedReader brI = new BufferedReader(frI);
					String lineIn;
					while ((lineIn = brI.readLine()) != null) {
					salesfiles.add(lineIn);
					}
					brI.close();

				} catch (IOException e ) {
					System.out.println(e);
				}
				String[] salessplit = Infile.getName().split("\\."); 	// "."の前には\\が必要
			//	System.out.println(salessplit[0]);
			//	salessplit[0]
				int No = Integer.parseInt(salessplit[0]);
				System.out.println(No);
			}
		}
	//	System.out.println(salesfiles);

			// ファイル名を分割し、リストに格納
			//	while (() != null) {


			// n+1 で連番がチェックできる
			// ループの終了をきちんと
	}
}
