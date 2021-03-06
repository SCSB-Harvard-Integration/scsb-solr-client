package org.recap.controller.swagger;

import io.swagger.annotations.*;
import org.recap.RecapConstants;
import org.recap.model.search.DataDumpSearchResult;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.search.SearchRecordsResponse;
import org.recap.model.search.SearchResultRow;
import org.recap.util.SearchRecordsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by sudhish on 13/10/16.
 */
@RestController
@RequestMapping("/searchService")
@Api(value="search", description="Search Records")
public class SearchRecordRestController {

    private static final Logger logger = LoggerFactory.getLogger(SearchRecordRestController.class);

    @Autowired
    private SearchRecordsUtil searchRecordsUtil=new SearchRecordsUtil();

    /**
     * Gets SearchRecordsUtil object.
     *
     * @return the SearchRecordsUtil object.
     */
    public SearchRecordsUtil getSearchRecordsUtil() {
        return searchRecordsUtil;
    }

    /**
     * This method searches books based on the given search records request parameter and returns a list of search result row.
     *
     * @param searchRecordsRequest the search records request
     * @return the SearchRecordsResponse.
     */
    @RequestMapping(value="/search", method = RequestMethod.POST)
    @ApiOperation(value = "search",notes = "Search Books in ReCAP - Using Method Post, Request data is String", nickname = "search")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Search")})
    @ResponseBody
    public SearchRecordsResponse searchRecordsServiceGetParam(@ApiParam(value = "Paramerters for Searching Records" , required = true, name="requestJson") @RequestBody SearchRecordsRequest searchRecordsRequest) {
        SearchRecordsResponse searchRecordsResponse = new SearchRecordsResponse();
        if(RecapConstants.CUSTOMER_CODE.equalsIgnoreCase(searchRecordsRequest.getFieldName())){
            searchRecordsRequest.setFieldValue(searchRecordsRequest.getFieldValue().toUpperCase());
        }
        try {
            List<SearchResultRow> searchResultRows = searchRecordsUtil.searchRecords(searchRecordsRequest);
            searchRecordsResponse.setSearchResultRows(searchResultRows);
            searchRecordsResponse.setTotalBibRecordsCount(searchRecordsRequest.getTotalBibRecordsCount());
            searchRecordsResponse.setTotalItemRecordsCount(searchRecordsRequest.getTotalItemRecordsCount());
            searchRecordsResponse.setTotalRecordsCount(searchRecordsRequest.getTotalRecordsCount());
            searchRecordsResponse.setTotalPageCount(searchRecordsRequest.getTotalPageCount());
            searchRecordsResponse.setShowTotalCount(searchRecordsRequest.isShowTotalCount());
            searchRecordsResponse.setErrorMessage(searchRecordsRequest.getErrorMessage());
        } catch (Exception e) {
            logger.info(RecapConstants.LOG_ERROR,e);
            searchRecordsResponse.setErrorMessage(e.getMessage());
        }
        return searchRecordsResponse;
    }

    /**
     * This method searches books based on the given search records request parameter and returns a list of DataDumpSearchResult which contains only bib ids and their corresponding item ids.
     *
     * @param searchRecordsRequest the search records request
     * @return the responseMap.
     */
    @RequestMapping(value="/searchRecords", method = RequestMethod.POST)
    @ApiOperation(value = "searchRecords",notes = "Search Books in ReCAP - Using Method Post, Request data is String", nickname = "searchRecords", consumes="application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Search")})
    public Map searchRecords(@ApiParam(value = "Paramerters for Searching Records" , required = true, name="requestJson") @RequestBody SearchRecordsRequest searchRecordsRequest) {
        List<DataDumpSearchResult> dataDumpSearchResults = null;
        Map responseMap = new HashMap();
        try {
            dataDumpSearchResults = getSearchRecordsUtil().searchRecordsForDataDump(searchRecordsRequest);
            responseMap.put("totalPageCount", searchRecordsRequest.getTotalPageCount());
            responseMap.put("totalRecordsCount", searchRecordsRequest.getTotalRecordsCount());
            responseMap.put("dataDumpSearchResults", dataDumpSearchResults);
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }
        return responseMap;
    }


    /**
     * This method searches books based on the given search parameters and returns a list of search result row.
     *
     * @param fieldValue                  the field value
     * @param fieldName                   the field name
     * @param owningInstitutions          the owning institutions
     * @param collectionGroupDesignations the collection group designations
     * @param availability                the availability
     * @param materialTypes               the material types
     * @param useRestrictions             the use restrictions
     * @param pageSize                    the page size
     * @return the SearchResultRow list.
     */
    @RequestMapping(value="/searchByParam", method = RequestMethod.GET)
    @ApiOperation(value = "searchParam",notes = "Search Books in ReCAP - Using Method GET, Request data as parameter", nickname = "search")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Search")})
    public List<SearchResultRow> searchRecordsServiceGet(
            @RequestParam(name="fieldValue", required = false)  String fieldValue,
            @ApiParam(name="fieldName",required = false,allowableValues = "Author_search,Title_search,TitleStartsWith,Publisher,PublicationPlace,PublicationDate,Subject,ISBN,ISSN,OCLCNumber,Notes,CallNumber_search,Barcode") @RequestParam(name="fieldName", value = "fieldName" , required = false)  String fieldName,
            @ApiParam(name="owningInstitutions", value= "Owning Institutions : PUL, CUL, NYPL")@RequestParam(name="owningInstitutions",required = false ) String owningInstitutions,
            @ApiParam(name="collectionGroupDesignations", value = "collection Designations : Shared,Private,Open") @RequestParam(name="collectionGroupDesignations", value = "collectionGroupDesignations" , required = false)  String collectionGroupDesignations,
            @ApiParam(name="availability", value = "Availability: Available, NotAvailable") @RequestParam(name="availability", value = "availability" , required = false)  String availability,
            @ApiParam(name="materialTypes", value = "MaterialTypes: Monograph, Serial, Other") @RequestParam(name="materialTypes", value = "materialTypes" , required = false)  String materialTypes,
            @ApiParam(name="useRestrictions", value = "Use Restrictions: NoRestrictions, InLibraryUse, SupervisedUse") @RequestParam(name="useRestrictions", value = "useRestrictions" , required = false)  String useRestrictions,
            @ApiParam(name="pageSize", value = "Page Size in Numers - 10, 20 30...") @RequestParam(name="pageSize", required = false) Integer pageSize
    ) {

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        if (fieldValue !=null) {
            searchRecordsRequest.setFieldValue(fieldValue);
        }
        if (fieldName !=null) {
            searchRecordsRequest.setFieldName(fieldName);
        }
        if(owningInstitutions !=null && owningInstitutions.trim().length()>0) {
            searchRecordsRequest.setOwningInstitutions(Arrays.asList(owningInstitutions.split(",")));
        }
        if(collectionGroupDesignations !=null && collectionGroupDesignations.trim().length()>0) {
            searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList(collectionGroupDesignations.split(",")));
        }
        if(availability !=null && availability.trim().length()>0) {
            searchRecordsRequest.setAvailability(Arrays.asList(availability.split(",")));
        }
        if(materialTypes !=null && materialTypes.trim().length()>0) {
            searchRecordsRequest.setMaterialTypes(Arrays.asList(materialTypes.split(",")));
        }
        if(pageSize !=null) {
            searchRecordsRequest.setPageSize(pageSize);
        }
        List<SearchResultRow> searchResultRows = null;
        try {
            searchResultRows = searchRecordsUtil.searchRecords(searchRecordsRequest);
        } catch (Exception e) {
            searchResultRows = new ArrayList<>();
            logger.error(RecapConstants.LOG_ERROR,e);
        }
        return searchResultRows;
    }
}
