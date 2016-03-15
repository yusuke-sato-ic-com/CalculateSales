package jp.co.iccom.sato_yusuke.calculate_sales;

import java.io.*;
import java.util.*;

public class CalculateSales {
	public static void main(String[] args) {
		// コマンドライン引数で、ディレクトリより支店定義ファイルを読み込む
		// ディレクトリまでのパス指定
		File branch = new File(args[0],"branch.lst"); // Fileオブジェクトの生成
		File commodity = new File(args[0],"commodity.lst");
		HashMap<String, String> branchMap = new HashMap<String, String>();
		HashMap<String, String> commodityMap = new HashMap<String, String>();

		try {
			FileReader fr1 = new FileReader(branch);
			BufferedReader br1 = new BufferedReader(fr1);
			String line1;
			while ((line1 = br1.readLine()) != null) {
				String[] branchData = line1.split(",");
			//	System.out.println(branchData[0] + branchData[1]);
				branchMap.put(branchData[0], branchData[1]);
			}
			br1.close(); //ストリームを閉じる
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
//			//	ここで集計用Mapの初期値0を格納した方がすっきりする
				commodityMap.put(commodityData[0], commodityData[1]);
			}
			br2.close();
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

		ArrayList<String> rcdFiles = new ArrayList<String>(); // rcdファイルを格納
		ArrayList<Integer> salesNo = new ArrayList<Integer>(); //

		// rcdファイルの抽出
		for (int i = 0; i < files.length; i++) { // 配列内の要素の数だけループし一覧を取得
			File inFile = files[i];
//			// 精度を上げる必要あり
			if (inFile.getName().endsWith(".rcd")) {
				rcdFiles.add(inFile.getName());
				String[] salesSplit = inFile.getName().split("\\."); 	// "."の前には\\が必要
				salesNo.add(Integer.parseInt(salesSplit[0]));
			}
		}

		// 連番チェック
		for (int i = 0; i < salesNo.size(); i++) {
			if(salesNo.get(i) != i + 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

		// 支店コード・商品コードをkey、金額をvalueに格納する集計用Mapを作成
//		// 定義ファイルの格納時点にまとめた方がすっきりする
		HashMap<String, Integer> rcdBranchSalesMap = new HashMap<String, Integer>();
		HashMap<String, Integer> rcdCommoditySalesMap = new HashMap<String, Integer>();

		// 支店コードをキーに、0をvalueに集計用Mapへ格納
		for(Map.Entry<String, String> e : branchMap.entrySet()) { // 拡張for文で、支店定義ファイルMapよりペアを取得
			rcdBranchSalesMap.put(e.getKey(), 0);
		}

		// 商品コードをkeyに、0をvalueに集計用Mapへ格納
		for(Map.Entry<String, String> e : commodityMap.entrySet()) { // 拡張for文で、商品定義ファイルMapよりペアを取得
			rcdCommoditySalesMap.put(e.getKey(), 0);
		}

		// rcdファイルの読み込み
		// Mapへ格納
		for (int i = 0; i < rcdFiles.size(); i++) {
			ArrayList<String> rcdDataList = new ArrayList<String>();
			try {
				FileReader fr = new FileReader(dir + "\\"+ rcdFiles.get(i));
				BufferedReader br = new BufferedReader(fr);
				String rcdLineInput;
				while ((rcdLineInput = br.readLine()) != null) {
					rcdDataList.add(rcdLineInput);
				}
				br.close();

			} catch (IOException e ) {
				System.out.println(e);
			}

			// 該当コード毎に売上金額を格納、加算
			String branchCodes = rcdDataList.get(0);
			String commodityCodes = rcdDataList.get(1);
			int amount = Integer.parseInt(rcdDataList.get(2));
// int型は桁数の問題あり

//			// 1つにまとめる！
			if (rcdBranchSalesMap.containsKey(branchCodes)) {
				int branchAmount = rcdBranchSalesMap.get(branchCodes) + amount;
				rcdBranchSalesMap.put(branchCodes, branchAmount);
			}

			if (rcdCommoditySalesMap.containsKey(commodityCodes)) {
				int commodityAmount = rcdCommoditySalesMap.get(commodityCodes) + amount;
				rcdCommoditySalesMap.put(commodityCodes, commodityAmount);
			}
		}
		System.out.println(rcdBranchSalesMap);
		System.out.println(rcdCommoditySalesMap);
	}
}
