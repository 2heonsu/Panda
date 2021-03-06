package com.kh.panda.board.controller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.kh.panda.attachment.model.vo.Attachment;
import com.kh.panda.board.model.service.BoardService;
import com.kh.panda.board.model.vo.Board;
import com.kh.panda.category.model.vo.Category;
import com.kh.panda.common.model.vo.MyFileRenamePolicy;
import com.kh.panda.common.model.vo.PageInfo;
import com.kh.panda.member.model.vo.Member;
import com.kh.panda.review.model.service.ReviewService;
import com.kh.panda.review.model.vo.Review;
import com.kh.panda.review.model.vo.ReviewFile;
import com.kh.panda.seller.model.vo.Seller;
@Controller
@RequestMapping("/board")
public class BoardController {
    @Autowired
    private BoardService boardService;
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping("/categoryBoard.do")
    public String selectList(@RequestParam(required = false) String mainCategory,
                             @RequestParam(required = false) String categoryName,
                             @RequestParam int currentPage,
                             @ModelAttribute PageInfo pageInfo,
                             @RequestParam(required = false, defaultValue = "BOARD_NO") String col,
                             @RequestParam(required = false, defaultValue = "ASC") String order,
                             @RequestParam(required = false) String type, 
                             @RequestParam(required = false) String keyword, 
                             HttpSession session,
                             Model model) {
        
        int listCount; // ?????? ?????????????????? ????????? ??? ?????? => BOARD ??? ?????? ?????? COUNT(*) ?????? (STATUS ='Y')
        // int currentPage; // ?????? ????????? (???, ???????????? ????????? ?????????) 
        int pageLimit; // ????????? ????????? ????????? ??????????????? ????????? ?????? ?????? => 10?????? ??????
        int boardLimit; // ??? ???????????? ????????? ???????????? ?????? ?????? 
        
        int maxPage; // ?????? ????????? ???????????? ?????? ??????????????? (== ??? ???????????? ??????) 
        int startPage; // ????????? ????????? ????????? ??????????????? ?????? ???
        int endPage; // ????????? ????????? ????????? ??????????????? ??? ???
        
        // * listCount : ??? ????????? ?????? 
    
        
        Map<String, String> forCount = new HashMap<>();
        forCount.put("mainCategory", mainCategory);
        forCount.put("categoryName", categoryName);
        
        listCount = boardService.selectListCount(forCount);
        
        // * pageLimit : ??????????????? ????????? ?????? ??????
        pageLimit = 10;
        
        // * boardLimit : ??? ???????????? ????????? ???????????? ?????? ??????
        boardLimit = 9;
        
        maxPage = (int)Math.ceil((double)listCount / boardLimit );
        
        startPage = (currentPage - 1) / pageLimit * pageLimit  + 1;
        
        endPage = startPage + boardLimit -1;
        
        if(endPage > maxPage) { 
            endPage = maxPage;
        }
        
        int startRow = (currentPage - 1) * boardLimit + 1;
        int endRow = startRow + boardLimit - 1;
        
        PageInfo pi = new PageInfo(listCount, currentPage, pageLimit, boardLimit, maxPage, startPage, endPage);
        Map<String, String> param = new HashMap<>();
        
        param.put("categoryName", categoryName);
        param.put("type", type);
        param.put("keyword", keyword);
        param.put("col", col);
        param.put("order", order);
        param.put("mainCategory", mainCategory);
        param.put("startRow", String.valueOf(startRow));
        param.put("endRow", String.valueOf(endRow));
        param.put("boardLimit", String.valueOf(boardLimit));
        List<Board> list = boardService.selectThumbnailList(param);
        
        if(((Member)session.getAttribute("loginUser")) == null) {
             // ????????? ?????? ??????
                   model.addAttribute("list", list);
                   model.addAttribute("pi", pi);
                   model.addAttribute("mainCategory", mainCategory);
                   model.addAttribute("categoryName", categoryName);
                   model.addAttribute("currentPage", currentPage);
         }
           else {
                  // ????????? ?????????
                 // ??????????????? ????????? ????????? ?????? model ??? ????????? ????????????
                // ????????? ???????????? model.addAttribute();???
                int memberNo = ((Member)session.getAttribute("loginUser")).getMemberNo();
                int result = boardService.checkSeller(memberNo);
               
                model.addAttribute("list", list);
                model.addAttribute("pi", pi);
                model.addAttribute("mainCategory", mainCategory);
                model.addAttribute("categoryName", categoryName);
                model.addAttribute("currentPage", currentPage);
                model.addAttribute("result", result);
         }
        	
        return "/board/categoryBoard";
    }
    
    @GetMapping("/enrollBoard.do")
    public String enrollBoard(HttpSession session,
                            Model model) {
        
        int memberNo = ((Member)session.getAttribute("loginUser")).getMemberNo();
        List<Seller> list = boardService.getInfo(memberNo);
        
        model.addAttribute("list", list);
        return "board/enrollBoard";
    }
    
    @PostMapping("/enrollBoard.do")
	public String enrollBoard(
			@ModelAttribute Board boardEnroll,
			@RequestParam String mainCategoryJsp,
			@RequestParam String categoryName,
			@RequestParam List<MultipartFile> upfiles,
			Model model,
			HttpSession session) {
		
		/*
		for(MultipartFile file : upfiles) {
			System.out.println(file.isEmpty());
			System.out.println(file.getOriginalFilename());
			System.out.println("---------");
		}
		*/

		String boardWriter = ((Member)session.getAttribute("loginUser")).getMemberNick();
		int memberNo = ((Member)session.getAttribute("loginUser")).getMemberNo();
		
		int sellerNo = boardService.getSellerNo(memberNo);
		
		Seller seller = boardService.sellerInfo(sellerNo);
	
		boardEnroll.setBoardWriter(boardWriter);	
		boardEnroll.setSellerNo(sellerNo);
		
		Map<String, String> param = new HashMap<>();
		param.put("mainCategory", String.valueOf(mainCategoryJsp));
		param.put("categoryName", String.valueOf(categoryName));
		int categoryNo = boardService.checkCategory(param);
		
		boardEnroll.setCategoryNo(categoryNo);
		
			ArrayList<Attachment> list = new ArrayList<>();
		
			//if(result > 0) {} // ??? ????????? ???????????? ??????????????? ????????? ?????????.
			String savePath = session.getServletContext().getRealPath("/resources/upfiles/");
			
				if(upfiles != null && !upfiles.get(0).isEmpty()) { // ??????????????? ?????????
				 // ?????????????????? ????????? ????????? ?????? ??????????????? ???????????????.(????????? ???????????? ?????? ?????????  Attachment ???????????? ??????) 
					
					int count = 0;
					
					for(int i = 0; i < 4; i ++) { 
						
						if(!upfiles.get(i).isEmpty()) {

							Attachment at = new Attachment();
							//at.setBoardNo(bdNo);
							
							if(count == 0) { // ????????? ???????????????
								// ????????? 
								count++;
								at.setFileLevel(1);
							}
							else { // ????????? ????????? ????????????
								at.setFileLevel(2);
							}
							
							at.setOriginName(upfiles.get(i).getOriginalFilename());
							String changeName = new MyFileRenamePolicy().rename(upfiles.get(i).getOriginalFilename());
							at.setChangeName(changeName);
							at.setFilePath("resources/upfiles/");
							at.setFileType(upfiles.get(i).getContentType());
							
							File target = new File(savePath, changeName);
							
							try {
								upfiles.get(i).transferTo(target);
							} catch (IllegalStateException | IOException e) {
								e.printStackTrace();
							}
							
							list.add(at);
						}
						else {
							
							Attachment at = new Attachment();
							at.setFileLevel(2);
							at.setOriginName("");
							at.setChangeName("");
							at.setFilePath("resources/upfiles/");
							at.setFileType("");
							
							list.add(at);
						}
					}
				}
				
				System.out.println(list);
	            int result = boardService.insertBoard(boardEnroll,list);
	            
	            if(result>0) {
	            	
	            	int boardNo = boardService.selectBoardNo(boardEnroll);
	            	
	            	Board board = new Board();
	            	board = boardService.detailContent(boardNo);
	            	            
		            List<Attachment> attachment = boardService.detailAttachment(boardNo);
		            
		            Category category = boardService.selectCategory(boardNo);
		            System.out.println(category);
		            
		            // ????????? ?????????
		            model.addAttribute("board", board);
		            model.addAttribute("attachment", attachment);
		            model.addAttribute("category", category);
		            model.addAttribute("boardNo",boardNo);
		
	                return "redirect:/board/detailView.do";
	            }
	            
	            else 
	            { return "redirect:/"; }  
	        
	    }
	 @GetMapping("/detailView.do")
	    public String detailView(@RequestParam int boardNo,
	    		@RequestParam(defaultValue="1") int currentPage,
				@RequestParam(defaultValue="1") int sort,
	                       HttpSession session,
	                             Model model) {
	       
	        int result = boardService.increaseCount(boardNo);
	        
	        if(result > 0) {
	            Board board = boardService.detailContent(boardNo);
	            
	            List<Attachment> attachment = boardService.detailAttachment(boardNo);
	            
	            Category category = boardService.selectCategory(boardNo);
	            System.out.println(category);
	            
	            int sellerNo = board.getSellerNo();
	            
	            Seller seller = boardService.sellerInfo(sellerNo);
	            
	            // ????????? ?????????
	            model.addAttribute("board", board);
	            model.addAttribute("seller", seller);
	            model.addAttribute("attachment", attachment);
	            model.addAttribute("category", category);
	            
	            
	            int pageLimit=5;
				int boardLimit=5; 
				int listCount;
				int maxPage; 
				int startPage; 
				int endPage; 
				
	       
	            //?????? 
	            //?????? ?????? ?????? 
	            if(session.getAttribute("loginUser") != null){
	    			int memberNo = ((Member)session.getAttribute("loginUser")).getMemberNo();
	    			int tradeCom=reviewService.tradeCom(memberNo,boardNo);
	    			model.addAttribute("tradeCom",tradeCom);
	    		}
	    		else {model.addAttribute("tradeCom",0);}
	            
	            //????????? ??????
	            
	            Map<String,String> sellerLoc = reviewService.sellerLoc(boardNo);
	           
	        	model.addAttribute("sellerLoc", sellerLoc);
	        	  
	        	
	            //?????? ??????
	            listCount=reviewService.selectListCount(boardNo);
	            maxPage = (int)Math.ceil((double)listCount / boardLimit);
	    		startPage = (currentPage - 1) / pageLimit * pageLimit + 1;
	    		endPage = startPage + pageLimit - 1;
	    		
	    		if(endPage > maxPage) {endPage = maxPage;}	
	    	
	            //???????????????
	            if(listCount==0) {
	            	model.addAttribute("listCount",0);
	            	model.addAttribute("avg",0);
	            	return "board/detailBoard";
	            }
	            model.addAttribute("listCount",listCount);
	            //?????? ?????????
	    		double avg=reviewService.avg(boardNo);
	    		model.addAttribute("avg",Math.round(avg*100) / 100.0);
	    		//etc
	    		model.addAttribute("currentPage",currentPage);
	    		model.addAttribute("sort",sort);
	    		model.addAttribute("maxPage",maxPage);
	    		model.addAttribute("startPage",startPage);
	    		model.addAttribute("endPage",endPage);
	            
	            
	          
	    		
	    		
	            // ????????? ??????
	            return "board/detailBoard";
	        } else {
	            return "redirect:categoryBoard.do";
	        }
	    }
    
    @GetMapping("/updateBoard.do")
    public String updateBoard(@RequestParam int boardNo,
                              Model model) {
        
        Board board = boardService.detailContent(boardNo);
        List<Attachment> attachment = boardService.detailAttachment(boardNo); 
        Category category = boardService.selectCategory(boardNo);
       
      
        model.addAttribute("board", board);
        model.addAttribute("attachment", attachment);
        model.addAttribute("category", category);
        
        return "board/updateBoard";
    }
    
    @PostMapping("/saveUpdateBoard.do")
    public String saveUpdateBoard(@ModelAttribute Board boardSave,
    		@RequestParam List<MultipartFile> newFileName,
            @RequestParam int boardNo,
            @RequestParam String mainCategoryJsp,
            @RequestParam String categoryName,
            @RequestParam List<MultipartFile> reupfiles,
            HttpSession session,
            Model model) {
        
    	for(MultipartFile f : newFileName) {
    		System.out.println(f.getOriginalFilename());
    	}
    	
        Map<String, String> param = new HashMap<>();
        param.put("mainCategory", String.valueOf(mainCategoryJsp));
        param.put("categoryName", String.valueOf(categoryName));
        int categoryNo = boardService.checkCategory(param);
        
		String boardWriter = ((Member)session.getAttribute("loginUser")).getMemberNick();
		int memberNo = ((Member)session.getAttribute("loginUser")).getMemberNo();
		
		int sellerNo = boardService.getSellerNo(memberNo);
		
		Seller seller = boardService.sellerInfo(sellerNo);
	
		boardSave.setBoardWriter(boardWriter);	
		boardSave.setSellerNo(sellerNo);
		
		boardSave.setCategoryNo(categoryNo);
        
        boardSave.setCategoryNo(categoryNo);

		// ?????? ?????? ????????? 
        List<Attachment> beforeAttachment = boardService.detailAttachment(boardNo); // ????????? ???????????? ???????????? ?????? ?????? ????????????

        //int resultDelete = boardService.deleteAttachment(boardNo);
        //System.out.println(resultDelete);
        
        ArrayList<Attachment> list1 = new ArrayList<>();
        ArrayList<Attachment> list = new ArrayList<>();
        String savePath = session.getServletContext().getRealPath("/resources/upfiles/");
		System.out.println(beforeAttachment);

		// ?????????
		int index = 0;

    	// ????????? ????????? ?????????
    	for(MultipartFile m : reupfiles) {
    		
    		Attachment at = new Attachment();
    		
    		// ?????????????????? ????????? ????????? ?????? ??????????????? ???????????????.(????????? ???????????? ?????? ?????????  Attachment ???????????? ??????) 
    		if(m.getSize() == 0) {
    			System.out.println("----- ???????????? -----");
    			
    			list1.add(beforeAttachment.get(index++));
    		}
    		else {
        		at.setBoardNo(boardNo);
                at.setOriginName(m.getOriginalFilename());
                String changeName = new MyFileRenamePolicy().rename(m.getOriginalFilename());
                at.setChangeName(changeName);
                at.setFilePath("resources/upfiles/");
                at.setFileType(m.getContentType());
                
                File target = new File(savePath, changeName);
				
				try {
					m.transferTo(target);
				} catch (IllegalStateException | IOException e) {
					e.printStackTrace();
				}
                
                System.out.println("----- ????????? -----");
                System.out.println(at);
                list.add(at);
    		}
    	}
    	
    	// ??? ???????????? ???????????? ?????????
    	System.out.println("?????? ?????? ?????????!");
    	for(Attachment a : list) {
    		System.out.println("---------------");
    		System.out.println(a);
    	}
    	
    	int result = boardService.updateBoard(boardSave, list);
            
		if(result>0) {
        	Board board = new Board();
        	board = boardService.detailContent(boardNo);
        	            
            List<Attachment> attachment = boardService.detailAttachment(boardNo);
            
            Category category = boardService.selectCategory(boardNo);
            System.out.println(category);
            
            // ????????? ?????????
            model.addAttribute("board", board);
            model.addAttribute("attachment", attachment);
            model.addAttribute("category", category);

            return "board/detailBoard";
        } else { 
        	return "redirect:/"; 
    	}
}
    
    @GetMapping("deleteBoard.do")
	public String deleteBoard(@RequestParam int boardNo,
							  @RequestParam String mainCategory,
							  @RequestParam String categoryName,
							  HttpSession session,
							  Model model) {
		
		Category category = boardService.selectCategory(boardNo);
		int result = boardService.deleteBoard(boardNo);
		
		
		if(result > 0) {
			session.setAttribute("alertMsg", "???????????? ?????? ???????????????.");
			model.addAttribute("category", category);
			return "redirect:/"; 
		} else {
			session.setAttribute("alertMsg", "????????? ????????? ??????????????????.");
			return "redirect:/"; 
		}
	}
	
  //?????? ????????? ?????????
  		@GetMapping("enroll.do")
  		public String enroll(
  				@RequestParam(required = false) int boardNo,
  				@RequestParam(required = false) int memberNo,
  				@RequestParam(required = false) String boardTitle,
  				 Model model
  				) {
  			    model.addAttribute("boardNo", boardNo);
  			    model.addAttribute("memberNo", memberNo);
  			    model.addAttribute("boardTitle", boardTitle);
  			
  			return "review/enroll";
  		}
  		
  		//?????? ?????? 
  		@PostMapping("/enroll.do")
  		public String enroll(
  				@ModelAttribute Review r,
  				@RequestParam(required = false) MultipartFile upfile1,
  				@RequestParam (required = false) MultipartFile upfile2,
  				 Model model,
  				HttpSession session) {
  		
  			//?????? ?????? ?????? ????????? ?????????
  			ArrayList<ReviewFile> fileList = new ArrayList<ReviewFile>();
  		
  			//1????????? vo??????
  			if(!upfile1.isEmpty()) {
  				//????????????
  				ReviewFile f=new ReviewFile();
  				f.setReviewNo(r.getReviewNo());
  				f.setOriginName(upfile1.getOriginalFilename());
  				//????????? ??????
  				String changeName = new MyFileRenamePolicy().rename(upfile1.getOriginalFilename());
  				f.setChangeName(changeName);
  				// ?????? ????????? ????????? ????????? ???????????? ??????
  				String filePath = session.getServletContext().getRealPath("/resources/reviewFiles/");
  				// DB??? FILEPATH ????????? ????????? ????????????
  				f.setFilePath("resources/reviewFiles/");
  				File target =new File(filePath,changeName);
  				try {
  					upfile1.transferTo(target);
  				}catch(IllegalStateException | IOException e) {
  				e.printStackTrace();
  				}
  				fileList.add(f);
  			}
  			
  			//2????????? vo??????
  			if(!upfile2.isEmpty()) {
  				//????????????
  				ReviewFile f=new ReviewFile();
  				f.setReviewNo(r.getReviewNo());
  				f.setOriginName(upfile2.getOriginalFilename());
  				//????????? ??????
  				String changeName = new MyFileRenamePolicy().rename(upfile2.getOriginalFilename());
  				f.setChangeName(changeName);
  				//???????????? ??????
  				String filePath = session.getServletContext().getRealPath("/resources/reviewFiles/");
  				f.setFilePath("resources/reviewFiles/");
  				File target =new File(filePath,changeName);
  				try {
  					upfile2.transferTo(target);
  				}catch(IllegalStateException | IOException e) {
  					e.printStackTrace();
  				}
  				fileList.add(f);
  			}
  		
  			int result = reviewService.enroll(r,fileList);
  			model.addAttribute("boardNo", r.getBoardNo());
  			switch(result){
  			case 1:
  			case 2:
  			case 3:
  				session.setAttribute("alertMsg", "????????? ??????????????? ?????????????????????.");
  				return "redirect:/board/detailView.do"; 
  			default:
  				session.setAttribute("alertMsg", "?????? ?????? ??????...????????? ?????? ????????? ?????????");
  				return "redirect:/board/detailView.do"; // union.do ??? ???????????????
  			}
  		
  		}
		
		
		 @GetMapping("sellerDetail.do")
		    public String sellerDetail(@RequestParam String boardWriter,
		    						   @RequestParam int currentPage,
		    						   Model model) {
		      	
		    	int memberNo = boardService.getInfoOfBoardWriter(boardWriter);
		    	int sellerNo = boardService.getSellerNo(memberNo);
		    	Seller seller = boardService.sellerInfo(sellerNo);
		    	System.out.println(seller);
		    	
		    	int listCount; // ?????? ?????????????????? ????????? ??? ?????? => BOARD ??? ?????? ?????? COUNT(*) ?????? (STATUS ='Y')
		        int pageLimit; // ????????? ????????? ????????? ??????????????? ????????? ?????? ?????? => 10?????? ??????
		        int boardLimit; // ??? ???????????? ????????? ???????????? ?????? ?????? 
		        
		        int maxPage; // ?????? ????????? ???????????? ?????? ??????????????? (== ??? ???????????? ??????) 
		        int startPage; // ????????? ????????? ????????? ??????????????? ?????? ???
		        int endPage; // ????????? ????????? ????????? ??????????????? ??? ???
		        
		        // * listCount : ??? ????????? ?????? 
		        listCount = boardService.selectBoardCount(boardWriter);
		        System.out.println(listCount);
		        // * pageLimit : ??????????????? ????????? ?????? ??????
		        pageLimit = 10;
		        
		        // * boardLimit : ??? ???????????? ????????? ???????????? ?????? ??????
		        boardLimit = 4;
		        
		        maxPage = (int)Math.ceil((double)listCount / boardLimit );
		        
		        startPage = (currentPage - 1) / pageLimit * pageLimit  + 1;
		        
		        endPage = startPage + boardLimit -1;
		        
		        if(endPage > maxPage) { 
		            endPage = maxPage;
		        }
		        
		        int startRow = (currentPage - 1) * boardLimit + 1;
		        int endRow = startRow + boardLimit - 1;
		        
		        PageInfo pi = new PageInfo(listCount, currentPage, pageLimit, boardLimit, maxPage, startPage, endPage);
		        System.out.println(pi);
		        
		        Map<String, String> param = new HashMap<>();
		        
		        param.put("startRow", String.valueOf(startRow));
		        param.put("endRow", String.valueOf(endRow));
		        param.put("boardLimit", String.valueOf(boardLimit));
		        param.put("sellerNo", String.valueOf(sellerNo));
		    	
		    	List<Board> list = boardService.selectThumbnailListByBoardNo(param);
		    	System.out.println(list);
		    	
		    	model.addAttribute("pi", pi);
		    	model.addAttribute("list", list);
		        model.addAttribute("currentPage", currentPage);
		    	model.addAttribute("boardWriter", boardWriter);
		    	model.addAttribute("seller", seller);
		    	System.out.println("??????");
		    	System.out.println(pi);
		    	System.out.println(list);
		    	System.out.println(currentPage);
		    	System.out.println(seller);
		    	
		    	return "board/sellerDetail";
		    	
		    }
		
		
		
		
		
		
}
    
	