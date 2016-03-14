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



		// ディレクトリ内のファイル一覧を取得
		String path = args[0];
		File dir = new File(path);
		File[] files = dir.listFiles(); // dir内のファイルを配列に格納

		ArrayList<String> rcdfiles = new ArrayList<String>(); // rcdファイルを格納
		ArrayList<Integer> salesNo = new ArrayList<Integer>(); //

		// rcdファイルの抽出
		for (int i = 0; i < files.length; i++) { // 配列内の要素の数だけループし一覧を取得
			File infile = files[i];
			// 精度を上げる必要あり
			if (infile.getName().endsWith(".rcd")) {
				rcdfiles.add(infile.getName());
				String[] salessplit = infile.getName().split("\\."); 	// "."の前には\\が必要
				salesNo.add(Integer.parseInt(salessplit[0]));
			}
		}

		// 連番チェック
		for (int i = 0; i < salesNo.size(); i++) {
			if(salesNo.get(i) != i + 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		// rcdファイルの読み込み
		// Mapへ格納
		HashMap<String, Integer> rcdbrachsalesmap = new HashMap<String, Integer>();
		HashMap<String, Integer> rcdcommoditysalesmap = new HashMap<String, Integer>();

		for (int i = 0; i < rcdfiles.size(); i++) {
			ArrayList<String> rcddet = new ArrayList<String>();
			try {
				FileReader fr = new FileReader(dir + "\\"+ rcdfiles.get(i));
				BufferedReader br = new BufferedReader(fr);
				String rcdlinein;
				while ((rcdlinein = br.readLine()) != null) {
					rcddet.add(rcdlinein);

				}
				br.close();

				rcdbrachsalesmap.put(rcddet.get(0) , Integer.parseInt(rcddet.get(2)));
				rcdcommoditysalesmap.put(rcddet.get(1), Integer.parseInt(rcddet.get(2)));

				//	  条件分岐で加算する方法
				//	○初期値０円のMapに加算する方法

			} catch (IOException e ) {
				System.out.println(e);
			}
		}
	//	System.out.println(rcdbrachsalesmap);
	//	System.out.println(rcdcommoditysalesmap);
	}
}
