package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.dao.DbConnection;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
	private Scanner scanner = new Scanner(System.in);
	private ProjectService projectsService = new ProjectService();
	private Project curProject;

	/* list of available options */
	// @formatter:off
	private List<String> operations = List.of(
			"1) Create and populate all tables",
			"2) Add a project",
			"3) List projects",
			"4) Select a project"
			
			
	);
	// @formatter:on
	/**
	 * Entry point for Java application
	 * 
	 * @param args Unused.
	 */
	public static void main(String[] args) {
		new ProjectsApp().displayMenu();

	}

	/**
	 * This method prints operations, gets user menu selection, and performs
	 * requested operation. It repeats until the user terminates the application.
	 */
	private void displayMenu() {
		boolean done = false;

		while (!done) {
			try {
				int operation = getOperation();

				switch (operation) {
				case -1:
					done = exitMenu();
					break;

				case 1:
					createTables();
					break;

				case 2:
					createProject();
					break;

				case 3:
					listProjects();
					break;

				case 4:
					selectProject();
					break;

				default:
					System.out.println("\n" + operation + " is not valid. Try again.");
					break;
				}
			} catch (Exception e) {
				System.out.println("\nError: " + e.toString() + " Try again.");
			}
		}
	}

	private void selectProject() {
		List<Project> projects = listProjects();

		Integer projectId = getIntInput("Select a project ID");
		
		// Unselect current project
		curProject = null;

		for (Project project : projects) {
			if (project.getProjectId().equals(projectId)) {
				curProject = projectsService.fetchProjectById(projectId);
				break;
			}
		}
		
		if(Objects.isNull(curProject)) {
			System.out.println("\nInvalid project selected.");
		}
	}

	/**
	 * fetch the list of projects, print the recipe IDs and names on the console,
	 * and return the list.
	 * 
	 * @return the list of projects
	 */
	private List<Project> listProjects() {
		List<Project> projects = projectsService.fetchAllProjects();

		System.out.println("\nProjects:");

		projects.forEach(
				project -> System.out.println("   " + project.getProjectId() + ": " + project.getProjectName()));

		return projects;
	}

	/**
	 * Gather user input for a project row then calls the project service to create
	 * row.
	 */
	private void createProject() {
		String projectName = getStringInput("Enter the project name");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
		String notes = getStringInput("Enter the project notes");

		/* Create a project object from the user input. */ 
		Project project = new Project();

		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);

		/**
		 * Add the project to the project table. This will throw unchecked exception if there's an error.
		 */
		Project dbProject = projectsService.addProject(project);
		System.out.println("You have successfully created project: " + dbProject);
		
		curProject = projectsService.fetchProjectById(dbProject.getProjectId());
	}

	private void createTables() {
		projectsService.createAndPopulateTables();
		System.out.println("\nTables created and populated!");
	}

	private boolean exitMenu() {
		System.out.println("\nExiting the menu.");
		return true;
	}

	private int getOperation() {
		printOperations();
		Integer op = getIntInput("\nEnter an operation number (press Enter to quit)");

		return Objects.isNull(op) ? -1 : op;
	}

	private void printOperations() {
		System.out.println();
		System.out.println("Here's what you can do:");

		operations.forEach(op -> System.out.println("   " + op));
		
		if(Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project.");
		} else {
			System.out.println("\nYou are working with project: " + curProject);
		}
	}

	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return new BigDecimal(input).setScale(2);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}

	private Double getDoubleInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}

		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String line = scanner.nextLine();

		return line.isBlank() ? null : line.trim();

	}

}