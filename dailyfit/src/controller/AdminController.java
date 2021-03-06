package controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bean.Board;
import bean.QuestionBoard;
import bean.ReplyBoard;
import bean.Sale;
import bean.Shop;
import dao.InfoDAO;
import dao.ShopDAO;
import hello.annotation.Mapping;
import hello.annotation.RootURL;
import hello.mv.ModelView;

@RootURL("/admin")
public class AdminController {

	@Mapping(url="/searchGoogle.ap")
	ModelView getSearchGooglePage(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/searchShopGoogle");
		return mv;
	}
	@Mapping(url="/event.ap")
	ModelView updateEvent(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/updateEvent");
		InfoDAO dao = new InfoDAO();
		Board board = dao.getEvent();
		mv.setModel("board", board);
		return mv;
	}
	@Mapping(url="/changeEvent.ap", method="post",bean="bean.Board")
	ModelView changeEvent(HttpServletRequest request,HttpServletResponse response,Object obj){
		ModelView mv = new ModelView("/jsonView");
		Board board = (Board) obj;
		System.out.println(board.getContents());
		System.out.println(board.getTitle());
		InfoDAO dao = new InfoDAO();
		dao.updateEvent(board);
		mv.setModel("check", "yes");
		return mv;
	}
	
	@Mapping(url="/geoLocation.ap")
	ModelView getFindGeoLocationPage(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/findGeoLocation");
		ShopDAO dao =new ShopDAO();
		ArrayList<Shop> shopList = dao.getAllShopInfo();
		mv.setModel("shopList", shopList);
		mv.setModel("size", shopList.size());

		return mv;
	}
	@Mapping(url="/addShop.ap")
	ModelView getAddShopPage(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/addShop");
		return mv;
	}
	@Mapping(url="/addShop.ap", method="post",bean="bean.Shop")
	ModelView registShop(HttpServletRequest request,HttpServletResponse response,Object bean){
		ModelView mv = new ModelView("/admin/addShop");
		Shop shop = (Shop) bean;
		System.out.println(shop.getMainImgUrl());
		ArrayList<Sale> saleList = new ArrayList<Sale>();
		for(String str : request.getParameterValues("checked")){
			if(str.equals("option1")){
				saleList.add(new Sale("D", "1", shop.getDprice()));
			}else if(str.equals("option2")){
				saleList.add(new Sale("C", shop.getC(), shop.getCprice()));
			}else if(str.equals("option3")){
				saleList.add(new Sale("M", shop.getM(), shop.getMprice()));
			}else if(str.equals("option4")) {
				saleList.add(new Sale("P", shop.getP(), shop.getPprice()));
			}
		}
		shop.setSaleList(saleList);
		ShopDAO dao = new ShopDAO();
		dao.addShop(shop);
		return mv;
	}
	
	@Mapping(url="/delShop.ap")
	ModelView getDelShopPage(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/delShop");
		return mv;
	}
	
	@Mapping(url="/addImage.ap")
	ModelView getAddImagePage(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/addImage");
		return mv;
	}
	
	@Mapping(url="/addImage.ap", method="post")
	ModelView getAddImagePagePost(HttpServletRequest request,HttpServletResponse response){
		System.out.println("imagePost 요청");
		ModelView mv = new ModelView("/admin/addImage");
		return mv;
	}
	@Mapping(url="/requestLoaction.ap", method="post")
	ModelView getLoation(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/locationJson");
		String address = request.getParameter("address");
		//여기부분 registGeoLocation과 중복 나중에처리
		try {
			address=URLEncoder.encode(address, "UTF-8");
			String url = "http://openapi.map.naver.com/api/geocode?key=b5168682c5a10f1e5b7592ae92fbb844&encoding=utf-8&coord=latlng&query="+address;
			URL mapXmlUrl = new URL(url);  //URL연결하고 받아오고 하는 부분들은 import가 필요하다. java.net.*
			HttpURLConnection urlConn = (HttpURLConnection)mapXmlUrl.openConnection();
			urlConn.setDoOutput(true);
			urlConn.setRequestMethod("POST");

			int len = urlConn.getContentLength();  //받아오는 xml의 길이

			if(len > 0){ 
				BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(),"utf-8"));
				String inputLine="";
				String mapxml = ""; //받아온 xml
				while((inputLine = br.readLine())!=null){
					mapxml += inputLine;  //한글자씩 읽어옵니다
				}
				if(mapxml != null){
					if(mapxml.indexOf("</item>") > -1 ){   //item이 있으면 좌표를 받아와야지
						String lat = "";  //위도
						String lon = "";  //경도
						lon = mapxml.substring( mapxml.indexOf("<x>")+3, mapxml.indexOf("</x>") ) ; //경도 잘라오기
						lat = mapxml.substring( mapxml.indexOf("<y>")+3, mapxml.indexOf("</y>") ) ; //위도 잘라오기
						mv.setModel("lat", Double.parseDouble(lat));
						mv.setModel("lng", Double.parseDouble(lon));
					}
				}
				br.close();  //버퍼리더 닫기
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return mv;
	}
	@Mapping(url="/registLocation.ap")
	ModelView registGeoLocation(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("redirect:/dailyfit/admin/geoLocation.ap");
		ShopDAO dao =new ShopDAO();
		ArrayList<Shop> shopList = dao.getAllShopInfo();

		for(Shop shop:shopList){
			try{
				String address = shop.getAddress();
				address=URLEncoder.encode(address, "UTF-8");
				String url = "http://openapi.map.naver.com/api/geocode?key=b5168682c5a10f1e5b7592ae92fbb844&encoding=utf-8&coord=latlng&query="+address;
				URL mapXmlUrl = new URL(url);  //URL연결하고 받아오고 하는 부분들은 import가 필요하다. java.net.*
				HttpURLConnection urlConn = (HttpURLConnection)mapXmlUrl.openConnection();
				urlConn.setDoOutput(true);
				urlConn.setRequestMethod("POST");

				int len = urlConn.getContentLength();  //받아오는 xml의 길이

				if(len > 0){ 
					BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(),"utf-8"));
					String inputLine="";
					String mapxml = ""; //받아온 xml
					while((inputLine = br.readLine())!=null){
						mapxml += inputLine;  //한글자씩 읽어옵니다
					}
					if(mapxml != null){
						if(mapxml.indexOf("</item>") > -1 ){   //item이 있으면 좌표를 받아와야지
							String lat = "";  //위도
							String lon = "";  //경도
							lon = mapxml.substring( mapxml.indexOf("<x>")+3, mapxml.indexOf("</x>") ) ; //경도 잘라오기
							lat = mapxml.substring( mapxml.indexOf("<y>")+3, mapxml.indexOf("</y>") ) ; //위도 잘라오기
							shop.setLat(lat);
							shop.setLng(lon);
							
						}
					}
					br.close();  //버퍼리더 닫기
				}
				System.out.println(shop.getLat()+", "+shop.getLng()+" : "+shop.getShopName()+", "+shop.getAddress());
				dao.updateLocation(shop);

			}catch(Exception e){
				e.printStackTrace();
			}
		}
		mv.setModel("shopList", shopList);
		mv.setModel("size", shopList.size());

		return mv;
	}
	@Mapping(url="/noticeList.ap")
	ModelView noticeView(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/noticeList");
		InfoDAO dao = new InfoDAO();
		ArrayList<Board> boardList = dao.getNotices();
		mv.setModel("boardList", boardList);
		return mv;
	}
	@Mapping(url="/addNotice.ap")
	ModelView addNotice(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/addNotice");		
		return mv;
	}
	
	@Mapping(url="/updateNotice.ap",method="post")
	ModelView updateNoticeContents(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("redirect:/dailyfit/admin/noticeList.ap");
		InfoDAO dao = new InfoDAO();
		Board updateTarget = new Board();
		updateTarget.setBoardNum(request.getParameter("noticeNo"));
		updateTarget.setTitle(request.getParameter("noticeTitle"));
		updateTarget.setContents(request.getParameter("noticeContents"));
		dao.updateNotice(updateTarget);
		
		return mv;
	}
	
	@Mapping(url="/deleteNotice.ap",method="post")
	ModelView deleteNoticeContents(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("redirect:/dailyfit/admin/noticeList.ap");
		InfoDAO dao = new InfoDAO();
		Board deleteTarget = new Board();
		System.out.println(request.getParameter("no"));
		deleteTarget.setBoardNum(request.getParameter("no"));
		dao.deleteNotice(deleteTarget);
		
		return mv;
	}
	
	@Mapping(url="/readNotice.ap",method="post")
	ModelView readNoticeContents(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/noticeUpdate");
		InfoDAO dao = new InfoDAO();
		Board target = new Board();
		target.setBoardNum(request.getParameter("no"));
		target = dao.getNotice(target);
		mv.setModel("noticeTarget", target);
		return mv;
	}
	
	@Mapping(url="/addNotice.ap",method="post")
	ModelView addNoticePost(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("redirect:/dailyfit/admin/noticeList.ap");
		InfoDAO dao = new InfoDAO();
		Board board = new Board();
		board.setTitle(request.getParameter("noticeTitle"));
		board.setContents(request.getParameter("noticeContents"));
		dao.insertNotice(board);
//		ArrayList<Board> boardList = dao.getNotices();
//		mv.setModel("boardList", boardList);
		return mv;
	}
	
	@Mapping(url="/questionOneByOne.ap")
	ModelView questionOneByOneView(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/questionOneByOne");
		InfoDAO dao = new InfoDAO();
		ArrayList<QuestionBoard> boardList = dao.getQuestionList();
		mv.setModel("boardList", boardList);		
		return mv;
	}
	
	@Mapping(url="/questionAD.ap")
	ModelView questionADView(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/questionAD");
		InfoDAO dao = new InfoDAO();
		ArrayList<Board> boardList = dao.getQuestionAD();
		mv.setModel("boardList", boardList);
		return mv;
	}
	
	@Mapping(url="/readAD.ap",method="post")
	ModelView readAD(HttpServletRequest request,HttpServletResponse response){
		ModelView mv = new ModelView("/admin/readAD");
		InfoDAO dao = new InfoDAO();
		Board target = new Board();
		target.setBoardNum(request.getParameter("no"));
		target = dao.getNotice(target);
		mv.setModel("noticeTarget", target);
		return mv;
	}
	
	
	
	@Mapping(url="/readQuestion.ap",method="post")
	ModelView readQuestionContents(HttpServletRequest request,HttpServletResponse response){		
		ModelView mv = new ModelView("/admin/readQuestion");
		InfoDAO dao = new InfoDAO();
		QuestionBoard target = null;
		String no = request.getParameter("no");
		target = dao.getQuestion(no);
		mv.setModel("target", target);
		return mv;
	}
	
	@Mapping(url="/insertReply.ap",method="post")
	ModelView insertRep(HttpServletRequest request,HttpServletResponse response){		
		System.out.println("insertReply 진입");
		ModelView mv = new ModelView("redirect:/dailyfit/admin/questionOneByOne.ap");
		InfoDAO dao = new InfoDAO();
		ReplyBoard target = new ReplyBoard();
		target.setContents(request.getParameter("replyContent"));
		target.setUserQuestionBoardNum(request.getParameter("noticeNo"));
		dao.insertReply(target);
		dao.updateRecieveRelpy(request.getParameter("noticeNo"));
		return mv;
	}
}
