package jp.co.iccom.sato_yusuke.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {

	static final String unknownError = "予期せぬエラーが発生しました";

	public static void main(String[] args) throws FileNotFoundException,IOException {
		// コマンドライン引数で指定したディレクトリより、定義ファイルを読み込む
		// ディレクトリまでのパス指定
		if( args.length == 0) { // コマンドライン引数に値がない場合
			System.out.println(unknownError);
			return;
		}

		File branch = new File(args[0],"branch.lst");
		File commodity = new File(args[0],"commodity.lst");

		// 定義ファイルのペアで保持するMap
		HashMap<String, String> branchMap = new HashMap<String, String>();
		HashMap<String, String> commodityMap = new HashMap<String, String>();

		// 支店コード・商品コードをkey、金額をvalueに格納する集計用Mapを作成
		HashMap<String, Long> rcdBranchSalesMap = new HashMap<String, Long>();
		HashMap<String, Long> rcdCommoditySalesMap = new HashMap<String, Long>();

		String siten = "支店";
		String shouhin = "商品";

		// 定義ファイルの読み込み 存在エラー フォーマットエラー チェック
		if(!readDefFile("branch.lst", siten, branch, branchMap,rcdBranchSalesMap, "^\\d{3}$")) {
			return;
		}
		if(!readDefFile("commodity.lst", shouhin, commodity, commodityMap,rcdCommoditySalesMap, "^\\w{8}$")) {
			return;
		}

		// 指定ディレクトリ内のファイル一覧を取得
		String path = args[0];
		File dir = new File(path);
		File[] files = dir.listFiles(); // dir内のファイルを配列に格納

		// 売上ファイルの格納List
		ArrayList<String> rcdFiles = new ArrayList<String>();
		ArrayList<Integer> salesNo = new ArrayList<Integer>();

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

		// 売上ファイルの読み込み、Listへ格納
		ArrayList<String> rcdDataList = null;
		for (int i = 0; i < rcdFiles.size(); i++) {
			rcdDataList = new ArrayList<String>();

			// 売上ファイルの読み込み、行数とフォーマットエラー
			if(!readRcd(dir, rcdFiles.get(i), rcdDataList)){
				return;
			}

			String branchCodes = rcdDataList.get(0);
			String commodityCodes = rcdDataList.get(1);
			long amount = Long.parseLong(rcdDataList.get(2));

			// 合計金額のエラーチェック
			if(!digitCheck(rcdBranchSalesMap, branchCodes, amount, rcdFiles.get(i), siten)){
				return;
			}
			if(!digitCheck(rcdCommoditySalesMap, commodityCodes, amount, rcdFiles.get(i), shouhin)){
				return;
			}
		}

		// 降順ソート 合計金額の降順に並び替える
		List<Map.Entry<String,Long>> branchSales = salesListSort(rcdBranchSalesMap);
		List<Map.Entry<String,Long>> commoditySales = salesListSort(rcdCommoditySalesMap);

		// 出力先ファイルの作成
		File branchOutputFile = new File(args[0], "branch.out");
		File commodityOutputFile = new File(args[0], "commodity.out");

		// 集計ファイルの出力
		if(!output(branchOutputFile, branchSales, branchMap)) {
			return;
		}
		if(!output(commodityOutputFile, commoditySales, commodityMap)) {
			return;
		}
	}

	// 【メソッド】支店定義ファイルの読み込み 存在エラー フォーマットエラーチェック
	// コードと0円を集計用Mapに格納
	static boolean readDefFile(String lst, String str, File defFile, HashMap<String, String> codeNameMap,
			HashMap<String, Long> codeSalesMap, String format) {
		if(!defFile.exists()){
			if(defFile.getName().matches(lst)){
				System.out.println(str + "定義ファイルが存在しません");
				return false;
			}
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(defFile));
			String line;
			while ((line = br.readLine()) != null) {
				String[] Data = line.split(",");
				// 正規表現によるフォーマットチェック
				if(defFile.getName().matches(lst)) {
					if (!Data[0].matches(format) || Data.length != 2) {
					System.out.println(str + "定義ファイルのフォーマットが不正です" );
					return false;
					}
				}
				codeNameMap.put(Data[0], Data[1]);
			}
		} catch (IOException e)  { // 例外を受け取る
			System.out.println(unknownError);
			return false;
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				System.out.println(unknownError);
				return false;
			}
		}
		// 支店コードをキーに、0をvalueに集計用Mapへ格納
		for(Map.Entry<String, String> e : codeNameMap.entrySet()) { // 拡張for文で、支店定義ファイルMapよりペアを取得
			codeSalesMap.put(e.getKey(), (long) 0);
		}
		return true;
	}

	// 【メソッド】売上ファイルの読み込み
	static boolean readRcd(File dir, String fileName, ArrayList<String> rcdDataList) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dir + File.separator + fileName));
			String rcdLineInput;
			while ((rcdLineInput = br.readLine()) != null) {
				rcdDataList.add(rcdLineInput);

			}
			// 売上ファイルの中身が4行以上の場合のエラー
			if((rcdDataList.size() != 3) || (!rcdDataList.get(2).matches("^\\d{0,10}$"))) {
				System.out.println("<" + fileName + ">のフォーマットが不正です");
				return false;
			}

		} catch (IOException e) {
			System.out.println(unknownError);
			return false;
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				System.out.println(unknownError);
				return false;
			}
		}
		return true;
	}

	// 該当コード毎に売上金額を加算
	// 【メソッド】合計金額の桁数エラーチェック
	static boolean digitCheck(HashMap<String, Long> codeSalesMap, String code,long amount, String fileName, String str){
		if (!codeSalesMap.containsKey(code)) {
			System.out.println("<" + fileName + ">の" + str + "コードが不正です");
			return false;
		}
		long totalAmount = codeSalesMap.get(code) + amount;
		codeSalesMap.put(code, totalAmount);
		// 合計金額が10桁を超えた場合のエラー
		int keta = Long.toString(totalAmount).length();
		if(keta > 10) {
			System.out.println("合計金額が10桁を超えました");
			return false;
		}
		return true;
	}

	//降順ソート
	static List<Map.Entry<String,Long>> salesListSort(HashMap<String,Long> map) {
			List<Map.Entry<String,Long>> sales = new ArrayList<Map.Entry<String,Long>>(map.entrySet());
		Collections.sort(sales, new Comparator<Map.Entry<String,Long>>() {

			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				return  ((Long)o2.getValue()).compareTo((Long)o1.getValue());
			}
		});
		return sales;
	}

	// 【メソッド】集計ファイルの出力
	static boolean output(File outputFile, List<Map.Entry<String,Long>> sortTotalSales, HashMap<String, String> codeNameMap) {
		BufferedWriter bw = null;
		try{
			FileWriter fw = new FileWriter(outputFile);
			bw = new BufferedWriter(fw);
			outputFile.createNewFile();
			for (Entry<String,Long> s : sortTotalSales) { // keyとvalueを拡張for文で取得。拡張for文の位置に注意！
				if (codeNameMap.containsKey(s.getKey())) {
					bw.write(s.getKey() + "," + codeNameMap.get(s.getKey()) + "," + s.getValue());
					bw.newLine();
				}
			}
		} catch(IOException e)  {
			System.out.println(unknownError);
			return false;
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				System.out.println(unknownError);
				return false;
			}
		}
		return true;
	}
}
