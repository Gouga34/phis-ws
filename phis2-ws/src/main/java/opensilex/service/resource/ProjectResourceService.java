//******************************************************************************
//                           ProjectResourceService.java 
// SILEX-PHIS
// Copyright © INRA 2017
// Creation date: March 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import opensilex.service.configuration.DateFormat;
import opensilex.service.configuration.DefaultBrapiPaginationValues;
import opensilex.service.configuration.GlobalWebserviceValues;
import opensilex.service.dao.ProjectDAO;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.resource.dto.project.ProjectDTO;
import opensilex.service.resource.dto.project.ProjectPostDTO;
import opensilex.service.resource.validation.interfaces.Date;
import opensilex.service.resource.validation.interfaces.Required;
import opensilex.service.resource.validation.interfaces.URL;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.view.brapi.Status;
import opensilex.service.view.brapi.form.AbstractResultForm;
import opensilex.service.view.brapi.form.ResponseFormGET;
import opensilex.service.view.brapi.form.ResponseFormPOST;
import opensilex.service.result.ResultForm;
import opensilex.service.model.Project;

/**
 * Project resource service.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
@Api("/projects")
@Path("projects")
public class ProjectResourceService extends ResourceService {
    
    /**
     * Project GET service.
     * @param limit
     * @param page
     * @param uri
     * @param name
     * @param acronyme
     * @param subprojectType
     * @param financialSupport
     * @param financialName
     * @param dateStart
     * @param dateEnd
     * @param keywords
     * @param parentProject
     * @param website
     * @return the projects found
     */
    @GET
    @ApiOperation(value = "Get all projects corresponding to the searched params given",
                  notes = "Retrieve all projects authorized for the user corresponding to the searched params given")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve all projects", response = Project.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                          dataType = "string", paramType = "header",
                          value = DocumentationAnnotation.ACCES_TOKEN,
                          example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectsBySearch(
    @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam("pageSize") @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int limit,
    @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam("page") @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page,
    @ApiParam(value = "Search by URI", example = DocumentationAnnotation.EXAMPLE_PROJECT_URI) @QueryParam("uri") @URL String uri,
    @ApiParam(value = "Search by name", example = DocumentationAnnotation.EXAMPLE_PROJECT_NAME) @QueryParam("name") String name,
    @ApiParam(value = "Search by acronyme", example = DocumentationAnnotation.EXAMPLE_PROJECT_ACRONYME) @QueryParam("acronyme") String acronyme,
    @ApiParam(value = "Search by subproject type", example = DocumentationAnnotation.EXAMPLE_PROJECT_SUBPROJECT_TYPE) @QueryParam("subprojectType") String subprojectType,
    @ApiParam(value = "Search by financial support", example = DocumentationAnnotation.EXAMPLE_PROJECT_FINANCIAL_SUPPORT) @QueryParam("financialSupport") String financialSupport,
    @ApiParam(value = "Search by financial name", example = DocumentationAnnotation.EXAMPLE_PROJECT_FINANCIAL_NAME) @QueryParam("financialName") String financialName,
    @ApiParam(value = "Search by date start", example = DocumentationAnnotation.EXAMPLE_PROJECT_DATE_START) @QueryParam("dateStart") @Date(DateFormat.YMD) String dateStart,
    @ApiParam(value = "Search by date end", example = DocumentationAnnotation.EXAMPLE_PROJECT_DATE_END) @QueryParam("dateEnd") @Date(DateFormat.YMD) String dateEnd,
    @ApiParam(value = "Searcg by keywords", example = DocumentationAnnotation.EXAMPLE_PROJECT_KEYWORDS) @QueryParam("keywords") String keywords,
    @ApiParam(value = "Search by parent project", example = DocumentationAnnotation.EXAMPLE_PROJECT_PARENT_PROJECT) @QueryParam("parentProject") String parentProject,
    @ApiParam(value = "Search by website", example = DocumentationAnnotation.EXAMPLE_PROJECT_WEBSITE) @QueryParam("website") @URL String website)  {
        
        ProjectDAO projectDao = new ProjectDAO();
        if (uri != null) {
            projectDao.uri = uri;
        }
        if (name != null) {
            projectDao.name = name;
        }
        if (acronyme != null) {
            projectDao.acronyme = acronyme;
        }
        if (subprojectType != null) {
            projectDao.subprojectType = subprojectType;
        }
        if (financialSupport != null) {
            projectDao.financialSupport = financialSupport;
        }
        if (financialName != null) {
            projectDao.financialName = financialName;
        }
        if (dateStart != null) {
            projectDao.dateStart = dateStart;
        }
        if (dateEnd != null) {
            projectDao.dateEnd = dateEnd;
        }
        if (keywords != null) {
            projectDao.keywords = keywords;
        }
        if (parentProject != null) {
            projectDao.parentProject = parentProject;
        }
        if (website != null) {
            projectDao.website = website;
        }
        
        projectDao.user = userSession.getUser();
        projectDao.setPageSize(limit);
        projectDao.setPage(page);
        
        return getProjectsData(projectDao);
    }
    
    /**
     * Single project GET service from URI.
     * @param projectURI
     * @param limit
     * @param page
     * @return the project found
     */
    @GET
    @Path("{projectURI}")
    @ApiOperation(value = "Get a project",
                  notes = "Retrieve a project. Need URL encoded project URI (Unique Resource Identifier)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Retrieve a project.", response = Project.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_FETCH_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                          dataType = "string", paramType = "header",
                          value = DocumentationAnnotation.ACCES_TOKEN,
                          example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectDetails(
    @ApiParam(value = DocumentationAnnotation.PROJECT_URI_DEFINITION, example = DocumentationAnnotation.EXAMPLE_PROJECT_URI, required = true) @PathParam("projectURI") @URL @Required String projectURI,
    @ApiParam(value = DocumentationAnnotation.PAGE_SIZE) @QueryParam("pageSize") @DefaultValue(DefaultBrapiPaginationValues.PAGE_SIZE) @Min(0) int limit,
    @ApiParam(value = DocumentationAnnotation.PAGE) @QueryParam("page") @DefaultValue(DefaultBrapiPaginationValues.PAGE) @Min(0) int page) {
        if (projectURI == null) {
            final Status status = new Status("Access error", StatusCodeMsg.ERR, "Empty Project URI");
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseFormGET(status)).build();
        }
        
        ProjectDAO projectDao = new ProjectDAO(projectURI);
        projectDao.setPageSize(limit);
        projectDao.setPage(page);
        
        projectDao.user = userSession.getUser();
        
        return getProjectsData(projectDao);
    }
    
    /**
     * Project POST service.
     * @param projects
     * @param context
     * @return the project creation result
     */
    @POST
    @ApiOperation(value = "Post a project",
                  notes = "Register a new project in the database")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Project saved", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 401, message = DocumentationAnnotation.USER_NOT_AUTHORIZED),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                          dataType = "string", paramType = "header",
                          value = DocumentationAnnotation.ACCES_TOKEN,
                          example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postProject(
            @ApiParam(value = DocumentationAnnotation.PROJECT_POST_DATA_DEFINITION) @Valid ArrayList<ProjectPostDTO> projects,
            @Context HttpServletRequest context) {
        AbstractResultForm postResponse = null;
        
        // If there is at least a project
        if (projects != null && !projects.isEmpty()) {
            ProjectDAO projectDao = new ProjectDAO();
            if (projectDao.remoteUserAdress != null) {
                projectDao.remoteUserAdress = context.getRemoteAddr();
            }

            projectDao.user = userSession.getUser();

            // Check and insert projects
            POSTResultsReturn result = projectDao.checkAndInsert(projects);

            if (result.getHttpStatus().equals(Response.Status.CREATED)) { //201: projects inserted
                postResponse = new ResponseFormPOST(result.statusList);
                postResponse.getMetadata().setDatafiles(result.getCreatedResources());
            } else if (result.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                    || result.getHttpStatus().equals(Response.Status.OK)
                    || result.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)
                    || result.getHttpStatus().equals(Response.Status.CONFLICT)) {
                postResponse = new ResponseFormPOST(result.statusList);
            }
            return Response.status(result.getHttpStatus()).entity(postResponse).build();
        } else {
            postResponse = new ResponseFormPOST(new Status("Request error", StatusCodeMsg.ERR, "Empty projects(s) to add"));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }
    
    /**
     * Project PUT service.
     * @param projects
     * @param context
     * @return the update result
     */
    @PUT
    @ApiOperation(value = "Update project")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Project updated", response = ResponseFormPOST.class),
        @ApiResponse(code = 400, message = DocumentationAnnotation.BAD_USER_INFORMATION),
        @ApiResponse(code = 404, message = "Project not found"),
        @ApiResponse(code = 500, message = DocumentationAnnotation.ERROR_SEND_DATA)
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", required = true,
                          dataType = "string", paramType = "header",
                          value = DocumentationAnnotation.ACCES_TOKEN,
                          example = GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putProject(
    @ApiParam(value = DocumentationAnnotation.PROJECT_POST_DATA_DEFINITION) @Valid ArrayList<ProjectDTO> projects,
    @Context HttpServletRequest context) {
        AbstractResultForm postResponse = null;
        
        if (projects != null && !projects.isEmpty()) {
            ProjectDAO projectDao = new ProjectDAO();
            if (projectDao.remoteUserAdress != null) {
                projectDao.remoteUserAdress = context.getRemoteAddr();
            }
            projectDao.user = userSession.getUser();
            
            // Check and update projects
            POSTResultsReturn result = projectDao.checkAndUpdateList(projects);
            
            if (result.getHttpStatus().equals(Response.Status.OK)) { //200: projects updated
                postResponse = new ResponseFormPOST(result.statusList);
                return Response.status(result.getHttpStatus()).entity(postResponse).build();
            } else if (result.getHttpStatus().equals(Response.Status.BAD_REQUEST)
                    || result.getHttpStatus().equals(Response.Status.OK)
                    || result.getHttpStatus().equals(Response.Status.INTERNAL_SERVER_ERROR)) {
                postResponse = new ResponseFormPOST(result.statusList);
            }
            return Response.status(result.getHttpStatus()).entity(postResponse).build();
        } else {
            postResponse = new ResponseFormPOST(new Status("Request error", StatusCodeMsg.ERR, "Empty projects(s) to update"));
            return Response.status(Response.Status.BAD_REQUEST).entity(postResponse).build();
        }
    }
        
    /**
     * Gets projects data.
     * @param projectDao
     * @return the projects found
     */
    private Response getProjectsData(ProjectDAO projectDao) {
        ArrayList<Project> projects = new ArrayList<>();
        ArrayList<Status> statusList = new ArrayList<>();
        ResultForm<Project> getResponse;
        Integer projectsCount = projectDao.count();
        
        if (projectsCount != null && projectsCount == 0) {
            getResponse = new ResultForm<>(projectDao.getPageSize(), projectDao.getPage(), projects, true, projectsCount);
            return noResultFound(getResponse, statusList);
        } else {
            projects = projectDao.allPaginate();
            
            if (projects == null || projectsCount == null) { //sql error
                getResponse = new ResultForm<>(0, 0, projects, true, projectsCount);
                return sqlError(getResponse, statusList);
            } else if (projects.isEmpty()) { // no result found
                getResponse = new ResultForm<>(projectDao.getPageSize(), projectDao.getPage(), projects, false, projectsCount);
                return noResultFound(getResponse, statusList);
            } else { //results founded
                getResponse = new ResultForm<>(projectDao.getPageSize(), projectDao.getPage(), projects, true, projectsCount);
                getResponse.setStatus(statusList);
                return Response.status(Response.Status.OK).entity(getResponse).build();
            }
        }
    }
}
