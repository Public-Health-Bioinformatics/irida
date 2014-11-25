package ca.corefacility.bioinformatics.irida.ria.web.projects;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ca.corefacility.bioinformatics.irida.model.event.ProjectEvent;
import ca.corefacility.bioinformatics.irida.model.event.SampleAddedProjectEvent;
import ca.corefacility.bioinformatics.irida.model.event.UserRemovedProjectEvent;
import ca.corefacility.bioinformatics.irida.model.event.UserRoleSetProjectEvent;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.service.ProjectEventService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

import com.google.common.collect.ImmutableMap;

@Controller
@RequestMapping("/events")
public class ProjectEventsController {
	Map<Class<? extends ProjectEvent>, String> fragmentNames = ImmutableMap.of(UserRoleSetProjectEvent.class,
			"user-role-event", UserRemovedProjectEvent.class, "user-removed-event", SampleAddedProjectEvent.class,
			"sample-added-event");
	private static final int PAGE_SIZE = 10;

	@Autowired
	private ProjectEventService eventService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private UserService userService;

	@RequestMapping("/project/{projectId}")
	public String getRecentEventsForProject(@PathVariable Long projectId, Model model) {
		Project project = projectService.read(projectId);

		Page<ProjectEvent> lastTenEventsForProject = eventService.getEventsForProject(project, new PageRequest(0,
				PAGE_SIZE, Direction.DESC, "createdDate"));
		List<Map<String, Object>> eventInfo = new ArrayList<>();

		for (ProjectEvent e : lastTenEventsForProject) {
			Map<String, Object> info = new HashMap<>();
			info.put("name", fragmentNames.get(e.getClass()));
			info.put("event", e);
			eventInfo.add(info);
		}

		model.addAttribute("events", eventInfo);

		return "events/events";
	}

	@RequestMapping("/current_user")
	public String getRecentEventsForUser(Model model, Principal principal) {
		String userName = principal.getName();
		User user = userService.getUserByUsername(userName);

		Page<ProjectEvent> lastTenEventsForProject = eventService.getEventsForUser(user, new PageRequest(0, PAGE_SIZE,
				Direction.DESC, "createdDate"));
		List<Map<String, Object>> eventInfo = new ArrayList<>();

		for (ProjectEvent e : lastTenEventsForProject) {
			Map<String, Object> info = new HashMap<>();
			info.put("name", fragmentNames.get(e.getClass()));
			info.put("event", e);
			eventInfo.add(info);
		}

		model.addAttribute("events", eventInfo);

		return "events/events";
	}
}
