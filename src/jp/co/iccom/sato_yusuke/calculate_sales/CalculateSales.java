package jp.co.iccom.sato_yusuke.calculate_sales;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class CalculateSales {

	public static void main(String[] args) throws FileNotFoundException,IOException {
		// コマンドライン引数で指定したディレクトリより、定義ファイルを読み込む
		// ディレクトリまでのパス指定
		 if( args.length == 0) { // コマンドライン引数に値がない場合
			 System.out.println("予期せぬエラーが発生しました");
			 return;
		 }

		File branch = new File(args[0],"branch.lst"); // Fileオブジェクトの生成
		File commodity = new File(args[0],"commodity.lst");

		HashMap<String, String> branchMap = new HashMap<String, String>();
		HashMap<String, String> commodityMap = new HashMap<String, String>();
		FileReader fr = null;
		BufferedReader br = null;

		// 支店定義ファイルの読み込み
		if(!branch.exists()){
			System.out.println("支店定義ファイルは存在しません");
			return;
		}
		try {
			fr = new FileReader(branch);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] branchData = line.split(",");
				// 正規表現によるフォーマットチェック
				if (!branchData[0].matches("^\\d{3}$") || branchData.length != 2) {
					System.out.println( "支店定義ファイルのフォーマットが不正です" );
					return;
				}
				branchMap.put(branchData[0], branchData[1]);
			}
		} catch (IOException e)  { // 例外を受け取る
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			br.close();
		}

		// 商品定義ファイルの読み込み
		if(!commodity.exists()){
			System.out.println("商品定義ファイルは存在しません");
			return;
		}
		try {
			fr = new FileReader(commodity);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] commodityData = line.split(",");
//			//	ここで集計用Mapの初期値0を格納した方がすっきりする
				if (!commodityData[0].matches("^\\w{8}$") || commodityData.length != 2 ) {
					System.out.println( "商品定義ファイルのフォーマットが不正です" );
					return;
				}
				commodityMap.put(commodityData[0], commodityData[1]);
			}
		} catch (IOException e)  { // 例外を受け取る
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			br.close();
		}

		// 指定ディレクトリ内のファイル一覧を取得
		String path = args[0];
		File dir = new File(path);
		File[] files = dir.listFiles(); // dir内のファイルを配列に格納

		ArrayList<String> rcdFiles = new ArrayList<String>(); // rcdファイルを格納
		ArrayList<Integer> salesNo = new ArrayList<Integer>(); //

		// 売上ファイルの抽出
		for (int i = 0; i < files.length; i++) { // 配列内の要素の数だけループし一覧を取得
			File inFile = files[i];
			if (inFile.getName().matches("^\\d{8}.rcd$") && inFile.isFile()) { // 数字8桁かつ拡張子がrcdのファイルのみ検索
				rcdFiles.add(inFile.getName());
				String[] salesSplit = inFile.getName().split("\\."); 	// "."の前には\\が必要
				salesNo.add(Integer.parseInt(salesSplit[0]));
			}
		}
		// 連番エラーのチェック
		for (int i = 0; i < salesNo.size(); i++) {
			if(salesNo.get(i) != i + 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

//		// 定義ファイルの格納時にまとめた方がすっきりする
		// 支店コード・商品コードをkey、金額をvalueに格納する集計用Mapを作成
		HashMap<String, Long> rcdBranchSalesMap = new HashMap<String, Long>();
		HashMap<String, Long> rcdCommoditySalesMap = new HashMap<String, Long>();

		// 支店コードをキーに、0をvalueに集計用Mapへ格納
		for(Map.Entry<String, String> e : branchMap.entrySet()) { // 拡張for文で、支店定義ファイルMapよりペアを取得
			rcdBranchSalesMap.put(e.getKey(), (long) 0);
		}

		// 商品コードをkeyに、0をvalueに集計用Mapへ格納
		for(Map.Entry<String, String> e : commodityMap.entrySet()) { // 拡張for文で、商品定義ファイルMapよりペアを取得
			rcdCommoditySalesMap.put(e.getKey(), (long) 0);
		}

		// 売上ファイルの読み込み
		// Listへ格納
		for (int i = 0; i < rcdFiles.size(); i++) {
			ArrayList<String> rcdDataList = new ArrayList<String>();
			try {
				fr = new FileReader(dir + "\\"+ rcdFiles.get(i));
				br = new BufferedReader(fr);
				String rcdLineInput;
				int lineCount = 0;
				while ((rcdLineInput = br.readLine()) != null) {
					// 売上ファイルの中身が4行以上の場合のエラー
					lineCount++;
					if(lineCount >= 4 || lineCount == 3 && !rcdLineInput.matches("^\\d{0,10}$")) {
						System.out.println("<" + rcdFiles.get(i) + ">のフォーマットが不正です");
						return;
					}
					rcdDataList.add(rcdLineInput);
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
			} finally {
				br.close();
			}

			// 該当コード毎に売上金額を格納、加算
			String branchCodes = rcdDataList.get(0);
			String commodityCodes = rcdDataList.get(1);
			long amount;
			try{
				amount = Long.parseLong(rcdDataList.get(2));
			} catch (NumberFormatException e) {
				System.out.println("合計金額が10桁を超えました");
				return;
			}

//			// 1つにまとめた方がいい？
			if (rcdBranchSalesMap.containsKey(branchCodes)) {
				long branchAmount = rcdBranchSalesMap.get(branchCodes) + amount;
				rcdBranchSalesMap.put(branchCodes, branchAmount);
				// 合計金額が10桁を超えた場合のエラー
				int keta = Long.toString(branchAmount).length();
				if(keta > 10) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			// 支店に該当がない場合のエラー
			} else {
				System.out.println("<" + rcdFiles.get(i) + ">の支店コードが不正です");
				return;
			}

			if (rcdCommoditySalesMap.containsKey(commodityCodes)) {
				long commodityAmount = rcdCommoditySalesMap.get(commodityCodes) + amount;
				rcdCommoditySalesMap.put(commodityCodes, commodityAmount);
				// 合計金額が10桁を超えた場合のエラー表示
				int keta = Long.toString(commodityAmount).length();
				if(keta > 10) {
					System.out.println("合計金額が10桁を超えました");
				return;
				}
			// 商品に該当がない場合のエラー
			} else {
				System.out.println("<" + rcdFiles.get(i) + ">の商品コードが不正です");
				return;
			}
		}

		// 支店別
		// 合計金額の降順に並び替える
		// ソート用List生成
		List<Map.Entry<String,Long>> branchSales =
			new ArrayList<Map.Entry<String,Long>>(rcdBranchSalesMap.entrySet());
		Collections.sort(branchSales, new Comparator<Map.Entry<String,Long>>() {

			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
			// TODO 自動生成されたメソッド・スタブ
			return  ((Long)o2.getValue()).compareTo((Long)o1.getValue());
			}
		});

		// 集計結果を出力する為、支店別集計ファイルの作成
		File newBranchOut = new File(args[0],"branch.out");
		try{
			newBranchOut.createNewFile();
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		// 支店別ファイルに出力
		FileWriter fw = null;;
		BufferedWriter bw = null;;
		try {
			fw = new FileWriter(newBranchOut);
			bw = new BufferedWriter(fw);
			for (Entry<String,Long> s : branchSales) { // keyとvalueを拡張for文で取得。拡張for文の位置に注意！
				if (branchMap.containsKey(s.getKey())) {
					bw.write(s.getKey() + "," + branchMap.get(s.getKey()) + "," + s.getValue());
					bw.newLine();
				}
			}
		} catch(IOException e)  {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			bw.close();
		}

		// 商品別
		// 合計金額の降順に並び替える
		// ソート用List生成
		List<Map.Entry<String,Long>> commoditySales =
			new ArrayList<Map.Entry<String,Long>>(rcdCommoditySalesMap.entrySet());
		Collections.sort(commoditySales, new Comparator<Map.Entry<String,Long>>() {

			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
			// TODO 自動生成されたメソッド・スタブ
			return  ((Long)o2.getValue()).compareTo((Long)o1.getValue());
			}
		});

		// 集計結果を出力する為、商品別集計ファイルの作成
		File newCommodityOut = new File(args[0],"commodity.out");
		try{
			newCommodityOut.createNewFile();
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		// 商品別ファイルに出力
		try {
			fw = new FileWriter(newCommodityOut);
			bw = new BufferedWriter(fw);
			for (Entry<String,Long> s : commoditySales) { // keyとvalueを拡張for文で取得。拡張for文の位置に注意！
				if (commodityMap.containsKey(s.getKey())) {
					bw.write(s.getKey() + "," + commodityMap.get(s.getKey()) + "," + s.getValue());
					bw.newLine();
				}
			}
		} catch(IOException e)  {
			System.out.println("予期せぬエラーが発生しました");
		} finally {
			bw.close();
		}
	}
}
