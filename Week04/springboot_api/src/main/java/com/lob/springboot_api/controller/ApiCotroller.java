package com.lob.springboot_api.controller;

import com.lob.springboot_api.dto.DaysTotalDto;
import com.lob.springboot_api.dto.RequestInfoDto;
import com.lob.springboot_api.resource.ResponseResource;
import com.lob.springboot_api.dto.UserDto;

import com.lob.springboot_api.service.RequestInfoSaveService;
import com.lob.springboot_api.service.UserAccessTotalService;
import com.lob.springboot_api.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Year;
import java.util.Calendar;
import java.util.List;

import static org.springframework.hateoas.server.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/users")
public class ApiCotroller {
    // API 요청 컨트롤러
    // request code = WB / code_explain = WRITEBOARD
    // requestID Generate value +1 / userID userID / requestCode = requestcode.requestCode / createDate = dateTime
    // requestService.requestForWriteBoard(UserDTO.getUserID,"WB") Logic을 진행할 때 DATE 생성 (datetime format YYMMDDhhmm)

    private final UserAccessTotalService userAccessTotalService;
    private final RequestInfoSaveService requestInfoSaveService;


    public ApiCotroller(UserAccessTotalService userAccessTotalService, RequestInfoSaveService requestInfoSaveService, UserAccountService userAccountService) {
        this.userAccessTotalService = userAccessTotalService;
        this.requestInfoSaveService = requestInfoSaveService;
    }

    /**
     * get 요청된 URL Resource (year)를 기준으로 DB 데이터의 통계를 뽑아오는 메서드
     * @author lob
     * @param year 검색 기준 (년도)
     * @return ResponseEntity에 responseResource에 setting한 결과값을 json 형태로 반환한다.
     */
    @GetMapping("/{year}")
    public ResponseEntity<Object> getYearAccessTotal(HttpSession httpSession, @PathVariable String year){
        //값 검증
        if(year.length() != 4){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Request {year} is incorrect Value.");
        }

        //검색한 대상의 정보를 저장
        //Path Variable이 없으므로 = 회원이 아니므로 (user id, name 등) guest 로 작성
        UserDto userDto = new UserDto.Builder()
                .userId("guest")
                .hr_Organ("None")
                .username("Guest")
                .build();

        //접속 기록 저장
        requestInfoSaveService.requestForWriteBoard(userDto.getUserID(),"WB");

        //DB 검색 결과
        List<RequestInfoDto> res = userAccessTotalService.findByYear(year.substring(2, 4));

        ResponseResource responseResource = new ResponseResource.Builder(String.valueOf(res.size())) // total count = 요청 수
                .title("2020년 접속자 통계")
                .description("2020년 기준 접속자 통계입니다.")
                .year(year)
                .month("ALL")
                .days("ALL")
                .build();

        responseResource.add(linkTo(ApiCotroller.class).slash(year).withSelfRel());
        responseResource.add(linkTo(HomeController.class).withRel("prev-link"));

        //Event Resource send
        return ResponseEntity.status(HttpStatus.OK).body(responseResource);
    }


    /**
     * get 요청된 URL Resource (year, month)를 기준으로 해당 년월의 DB 데이터의 통계를 뽑아오는 메서드
     * @author lob
     * @param year,month 검색 기준 (년도, 월)
     * @return ResponseEntity에 responseResource에 setting한 결과값을 json 형태로 반환한다.
     */
    @GetMapping("/{id}/{year}/{month}")
    public ResponseEntity<Object> getMonthAccessTotal(HttpServletRequest request, HttpSession httpSession , @PathVariable String id,
                                                      @PathVariable String year, @PathVariable String month) {
        if (year.length() != 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Request {year} is incorrect Value.");
        }
        if (month.length() != 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Request {month} is incorrect Value.");
        }

        //로그인 세션값 저장.
        httpSession = request.getSession(true);

        //PathVariable로 보내진 ID가 로그인 된 세선 정보(ID)인지 확인.
        UserDto user = (UserDto) httpSession.getAttribute("user");
        if(!user.getUserID().equals(id)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Value for User dosn't Login.");
        }

        requestInfoSaveService.requestForWriteBoard(id,"WB");

        List<RequestInfoDto> res = userAccessTotalService.findByMonths(year.substring(2, 4), month);

        ResponseResource responseResource = new ResponseResource.Builder(String.valueOf(res.size()))
                .title(year+"년 접속 요청 통계")
                .description(year+"년 "+month.replace("0","")+"월 기준 접속 요청 통계입니다.")
                .year(year)
                .month(month)
                .days("ALL")
                .build();

        responseResource.add(linkTo(ApiCotroller.class).slash(id).slash(year).slash(month).withSelfRel());
        responseResource.add(linkTo(HomeController.class).withRel("prev-link"));

        return ResponseEntity.status(HttpStatus.OK).body(responseResource);
    }


    /**
     * get 요청된 URL Resource (year, month, day)를 기준으로 해당 년월일의 DB 데이터의 통계를 뽑아오는 메서드
     * @author lob
     * @param year,month,day 검색 기준 (년도, 월, 일)
     * @return ResponseEntity에 responseResource에 setting한 결과값을 json 형태로 반환한다.
     */
    @GetMapping("{id}/{year}/{month}/{day}")
    public ResponseEntity<Object> getDaysAccessTotal(HttpServletRequest request, HttpSession httpSession ,
                                                     @PathVariable String id,@PathVariable String year,
                                                     @PathVariable String month,@PathVariable String day) {

        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(year) , (Integer.parseInt(month)-1), Integer.parseInt(day));
        int maxDay = Integer.parseInt(String.valueOf(cal.getActualMaximum(Calendar.DAY_OF_MONTH)));

        if (year.length() != 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Request {year} is incorrect Value.");
        }
        if (month.length() != 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Request {month} is incorrect Value.");
        }
        if (Integer.parseInt(day) > maxDay || Integer.parseInt(day) == 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Request {day} is incorrect Value.");
        }

        //로그인 세션값 저장.
        httpSession = request.getSession(true);

        //PathVariable로 보내진 ID가 로그인 된 세선 정보(ID)인지 확인.
        UserDto user = (UserDto) httpSession.getAttribute("user");
        if(!user.getUserID().equals(id)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Value for User dosn't Login.");
        }

        requestInfoSaveService.requestForWriteBoard(id,"WB");

        List<RequestInfoDto> res = userAccessTotalService.findByDays(year.substring(2, 4), month, day);

        ResponseResource responseResource = new ResponseResource.Builder(String.valueOf(res.size()))
                .title(year+"년 "+month+"월 "+day+"일 기준 요청 통계")
                .description(year+"년 "+month.replace("0","")+"월 "+day+"일 기준 접속 요청 통계입니다.")
                .year(year)
                .month(month)
                .days(day)
                .build();

        responseResource.add(linkTo(ApiCotroller.class).slash(year).slash(month).slash(day).withSelfRel());
        responseResource.add(linkTo(HomeController.class).withRel("prev-link"));

        return ResponseEntity.status(HttpStatus.OK).body(responseResource);
    }


    /**
     * DB 저장 데이터를 기준으로 모든 접속 데이터의 일자별 평균 통계를 뽑아오는 메서드
     * @author lob
     * @param request URI get 요청
     * @return ResponseEntity에 responseResource에 setting한 결과값을 json 형태로 반환한다.
     */
    @GetMapping("{id}/day/average")
    public ResponseEntity<Object> getDaysAverageTotal(HttpServletRequest request, HttpSession httpSession ,
                                                      @PathVariable String id){

        httpSession = request.getSession(true);

        UserDto user = (UserDto) httpSession.getAttribute("user");
        if(!user.getUserID().equals(id)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Value for User dosn't Login.");
        }

        List<DaysTotalDto> res = userAccessTotalService.findByDaysAverage();
        long dateGroup = res.size();
        long requestCount = 0;

        for (DaysTotalDto re : res) {
            long data = re.getCount();
            requestCount += data;
        }
        BigDecimal valueAverage = new BigDecimal(requestCount).divide(BigDecimal.valueOf(dateGroup), MathContext.DECIMAL32);

        ResponseResource responseResource = new ResponseResource.Builder(String.valueOf(valueAverage))
                .title("모든 기간 평균 접속 요청 통계")
                .description("모든 기간 평균 접속 요청 통계입니다.")
                .year("ALL")
                .month("ALL")
                .days("ALL")
                .build();

        responseResource.add(linkTo(ApiCotroller.class).slash(id).slash("day").slash("average").withSelfRel());
        responseResource.add(linkTo(HomeController.class).withRel("prev-link"));

        requestInfoSaveService.requestForWriteBoard(id,"WB");

        return ResponseEntity.status(HttpStatus.OK).body(responseResource);
    }

    /**
     * DB 저장 데이터를 기준으로 휴일을 제외한 모든 접속 데이터의 일자별 평균 통계를 뽑아오는 메서드
     * @author lob
     * @param request URI get 요청
     * @return ResponseEntity에 responseResource에 setting한 결과값을 json 형태로 반환한다.
     */
    @GetMapping("{id}/day/average/not-holiday")
    public ResponseEntity<Object> getDaysIsNotHolidayAverageTotal(HttpServletRequest request, HttpSession httpSession ,
                                                                  @PathVariable String id){

        httpSession = request.getSession(true);

        UserDto user = (UserDto) httpSession.getAttribute("user");
        if(!user.getUserID().equals(id)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Value for User dosn't Login.");
        }

        List<DaysTotalDto> res = userAccessTotalService.findByNotholidayAverage();
        long dateGroup = res.size();
        long requestCount = 0;

        for (DaysTotalDto re : res) {
            long data = re.getCount();
            requestCount += data;
        }
        BigDecimal valueAverage = new BigDecimal(requestCount).divide(BigDecimal.valueOf(dateGroup), MathContext.DECIMAL32);

        ResponseResource responseResource = new ResponseResource.Builder(String.valueOf(valueAverage))
                .title("휴일을 제외한 모든 기간 평균 접속 요청 통계")
                .description("휴일을 제외한 모든 기간 접속 요청 통계입니다.")
                .year("ALL")
                .month("ALL")
                .days("ALL")
                .build();

        responseResource.add(linkTo(ApiCotroller.class).slash(id).slash("day").slash("average").slash("not-holiday").withSelfRel());
        responseResource.add(linkTo(HomeController.class).withRel("prev-link"));

        requestInfoSaveService.requestForWriteBoard(id,"WB");

        return ResponseEntity.status(HttpStatus.OK).body(responseResource);
    }

    /**
     * get 요청된 URL Resource (year, month, Oragan)를 기준으로 해당 년월의 부서 접속 데이터를 뽑아오는 메서드
     * @author lob
     * @param year,month,organ 검색 기준 (년도, 월, 부서)
     * @return ResponseEntity에 responseResource에 setting한 결과값을 json 형태로 반환한다.
     */
    @GetMapping("{id}/{year}/{month}/dept/{organ}")
    public ResponseEntity<Object> getMonthAndOrganTotal(HttpServletRequest request, HttpSession httpSession ,
                                                       @PathVariable String id,@PathVariable String year,
                                                       @PathVariable String month,@PathVariable String organ){

        if (year.length() != 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Request {year} is incorrect Value.");
        }
        if (month.length() != 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This Request {month} is incorrect Value.");
        }
        organ = organ.replace("#20", " ");
        List<RequestInfoDto> res = userAccessTotalService.findByMonthAndOrgan(year.substring(2, 4), month, organ);

        ResponseResource responseResource = new ResponseResource.Builder(String.valueOf(res.size()))
                .title(year+"년 "+month+"월 "+organ+"부서 접속 요청 통계")
                .description(year+"년 "+month.replace("0","")+"월 "+organ+"부서 기준 요청 통계입니다.")
                .year(year)
                .month(month)
                .days("ALL")
                .build();

        responseResource.add(linkTo(ApiCotroller.class).slash(id).slash(year).slash(month).slash("dept").slash(organ).withSelfRel());
        responseResource.add(linkTo(HomeController.class).withRel("prev-link"));

        httpSession = request.getSession(true);

        UserDto user = (UserDto) httpSession.getAttribute("user");
        if(!user.getUserID().equals(id)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Value for User dosn't Login.");
        }

        requestInfoSaveService.requestForWriteBoard(id,"WB");

        return ResponseEntity.status(HttpStatus.OK).body(responseResource);
    }

    /**
     * get 요청된 URL Resource (Oragan)를 기준으로 모든 부서 접속 데이터를 뽑아오는 메서드
     * @author lob
     * @param organ 검색 기준 (부서)
     * @return ResponseEntity에 responseResource에 setting한 결과값을 json 형태로 반환한다.
     */
    @GetMapping("{id}/dept/{organ}")
    public ResponseEntity<Object> getOrganTotal(HttpServletRequest request, HttpSession httpSession ,
                                                       @PathVariable String id,@PathVariable String organ){


        organ = organ.replace("#20", " ");
        List<RequestInfoDto> res = userAccessTotalService.findByOrgan(organ);

        ResponseResource responseResource = new ResponseResource.Builder(String.valueOf(res.size()))
                .title("모든 기간 "+organ+"부서 요청 통계")
                .description("모든 기간 "+organ+"부서 요청 통계입니다.")
                .year("ALL")
                .month("ALL")
                .days("ALL")
                .build();

        responseResource.add(linkTo(ApiCotroller.class).slash(id).slash("dept").slash(organ).withSelfRel());
        responseResource.add(linkTo(HomeController.class).withRel("prev-link"));

        httpSession = request.getSession(true);

        UserDto user = (UserDto) httpSession.getAttribute("user");
        if(!user.getUserID().equals(id)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Value for User dosn't Login.");
        }

        requestInfoSaveService.requestForWriteBoard(id,"WB");

        return ResponseEntity.status(HttpStatus.OK).body(responseResource);
    }

}
