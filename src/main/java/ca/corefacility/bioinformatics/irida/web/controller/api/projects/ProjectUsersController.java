package ca.corefacility.bioinformatics.irida.web.controller.api.projects;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.Relationship;
import ca.corefacility.bioinformatics.irida.model.Role;
import ca.corefacility.bioinformatics.irida.model.User;
import ca.corefacility.bioinformatics.irida.model.roles.impl.Identifier;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.UserService;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.ResourceCollection;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.RootResource;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.user.UserResource;
import ca.corefacility.bioinformatics.irida.web.controller.api.GenericController;
import ca.corefacility.bioinformatics.irida.web.controller.api.RelationshipsController;
import ca.corefacility.bioinformatics.irida.web.controller.api.UsersController;
import com.google.common.net.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collection;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Controller for managing relationships between {@link Project} and {@link User}.
 *
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
@Controller
public class ProjectUsersController {

    /**
     * key used in map when adding user to project.
     */
    public static final String USER_ID_KEY = "userId";
    /**
     * Rel to get to the list of users associated with a project.
     */
    public static final String REL_PROJECT_USERS = "project/users";
    /**
     * Reference to {@link UserService} for managing {@link User}.
     */
    private UserService userService;
    /**
     * Reference to {@link ProjectService} for managing {@link Project}.
     */
    private ProjectService projectService;

    protected ProjectUsersController() {
    }

    @Autowired
    public ProjectUsersController(UserService userService, ProjectService projectService) {
        this.userService = userService;
        this.projectService = projectService;
    }

    /**
     * Get all users associated with a project.
     *
     * @param projectId the project id to get users for.
     * @return a model with a collection of user resources.
     */
    @RequestMapping(value = "/projects/{projectId}/users", method = RequestMethod.GET)
    public ModelMap getUsersForProject(@PathVariable String projectId) {
        ResourceCollection<UserResource> resources = new ResourceCollection<>();

        // get all of the users belonging to this project
        Collection<Relationship> relationships = userService.getUsersForProject(new Identifier(projectId));

        // for each of those relationships, retrieve the complete user object
        // and convert to a resource suitable for sending back to the client.
        for (Relationship r : relationships) {
            User u = userService.getUserByUsername(r.getSubject().getIdentifier());
            UserResource ur = new UserResource(u);
            ur.add(linkTo(UsersController.class).slash(u.getIdentifier().getIdentifier()).withSelfRel());
            ur.add(linkTo(RelationshipsController.class).
                    slash(r.getIdentifier().getIdentifier()).withRel(RelationshipsController.REL_RELATIONSHIP));

            resources.add(ur);
        }

        // add a link to this resource to the response
        resources.add(
                linkTo(methodOn(ProjectUsersController.class, String.class).getUsersForProject(projectId)).withSelfRel());

        // prepare the response for the client
        ModelMap model = new ModelMap();
        model.addAttribute(GenericController.RESOURCE_NAME, resources);

        return model;
    }

    /**
     * Add a relationship between a {@link ca.corefacility.bioinformatics.irida.model.Project} and a {@link User}.
     *
     * @param representation the JSON key-value pair that contains the identifier for the project and the identifier for
     *                       the user.
     * @return a response indicating that the collection was modified.
     */
    @RequestMapping(value = "/projects/{projectId}/users", method = RequestMethod.POST)
    public ResponseEntity<String> addUserToProject(@PathVariable String projectId,
                                                   @RequestBody Map<String, String> representation) {
        // first, get the project
        Project p = projectService.read(new Identifier(projectId));

        String username = representation.get(USER_ID_KEY);

        // then, get the user
        User u = userService.getUserByUsername(username);
        Role r = new Role("ROLE_USER");

        // then add the user to the project with the specified role.
        projectService.addUserToProject(p, u, r);

        String location = linkTo(ProjectsController.class).slash(projectId).slash("users").slash(username).withSelfRel().getHref();

        MultiValueMap<String, String> responseHeaders = new LinkedMultiValueMap<>();
        responseHeaders.add(HttpHeaders.LOCATION, location);

        return new ResponseEntity<>("success", responseHeaders, HttpStatus.CREATED);
    }

    /**
     * Remove the relationship between a {@link Project} and a {@link User}.
     *
     * @param projectId the {@link Project} identifier to remove the {@link User} from.
     * @param userId    the {@link User} identifier to remove from the {@link Project}.
     * @return a response including links back to the {@link Project} and the {@link User} collection for
     *         the {@link Project}.
     */
    @RequestMapping(value = "/projects/{projectId}/users/{userId}", method = RequestMethod.DELETE)
    public ModelMap removeUserFromProject(@PathVariable String projectId, @PathVariable String userId) {
        // Read the project and user from the database
        Project p = projectService.read(new Identifier(projectId));
        User u = userService.getUserByUsername(userId);

        // ask the project service to remove the user from the project
        projectService.removeUserFromProject(p, u);

        // prepare a link back to the user collection of the project
        RootResource response = new RootResource();
        response.add(linkTo(methodOn(ProjectUsersController.class).getUsersForProject(projectId))
                .withRel(REL_PROJECT_USERS));

        // prepare a link back to the project
        response.add(linkTo(ProjectsController.class).slash(projectId)
                .withRel(ProjectsController.REL_PROJECT));

        // respond to the client
        ModelMap modelMap = new ModelMap();
        modelMap.addAttribute(GenericController.RESOURCE_NAME, response);

        return modelMap;
    }
}
