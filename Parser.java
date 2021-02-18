import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;


public class Parser {
	
	private static void login(WebDriver driver, String name, String pass) throws InterruptedException {
		//login into leetcode
		driver.get("https://leetcode.com/accounts/login/");
		Thread.sleep(1000); 
		driver.findElement(By.name("login")).sendKeys(name);
		Thread.sleep(500);
		driver.findElement(By.name("password")).sendKeys(pass);
		Thread.sleep(500);
	    driver.findElement(By.xpath(
	            "//button[@class='btn__2_DK fancy-btn__1eV0 primary__1l3Z light__3h9J btn__2ZIK btn-md__1Zv4 ']")).click();
	    Thread.sleep(3000);
	}
	
	private static List<String> getExerciseLinks(String fileLocation, WebDriver driver) throws InterruptedException {
		System.out.println("Getting exercise hyperlinks");
		driver.get("https://leetcode.com/submissions/");
		
		Thread.sleep(3000); 
		HashSet<String> allreadyAnsweredQuestions = new HashSet<>();
		
		PrintWriter fileOut = null;
		try {
			fileOut = new PrintWriter(fileLocation + "submissions.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> submissions = new LinkedList<>();
		
		while(true) {
			List<WebElement> tableRows = driver.findElements(By.xpath("//table/tbody/tr"));
			//get submission links
			for(WebElement row: tableRows) {
				String status = row.findElements(By.tagName("td")).get(2).getText();
				if (!status.equals("Accepted")) //we will note down only accepted answers
					continue;
				String qName = row.findElements(By.tagName("td")).get(1).getText();
				if (allreadyAnsweredQuestions.contains(qName)) //we will take only the last submission for each question
					continue;
				allreadyAnsweredQuestions.add(qName);
				String submissionLink = row.findElements(By.tagName("a")).get(0).getAttribute("href");
				fileOut.append(qName+ " " + submissionLink + "\n");
				submissions.add(submissionLink);

			}
			//check if next page exists and if it does go there
			try {
				String next_page = driver.findElements(By.xpath("//nav/ul/li[2]"
						)).get(0).findElements(By.tagName("a")).get(0).
						getAttribute("href");
				driver.get(next_page);
				System.out.println(next_page);
				Thread.sleep(1000);
			}
			catch (IndexOutOfBoundsException e) {
				System.out.println("No next page found (all pages done)");
				break;
			}
			catch (Exception e) {
				System.out.println(e);
				break;
			};

		}
		fileOut.close();
		return submissions;
	}
	
	private static void getExercises(String fileLocation, WebDriver driver, List<String> submissions) throws InterruptedException {
		System.out.println("Printing exercises codes into files");
		
		HashMap<String, String []> extention = new HashMap<>() ;
		extention.put("cpp", new String [] {".cpp", "/*", "*/"});
		extention.put("java", new String [] {".java", "/*", "*/"});
		extention.put("python", new String [] {".py", "'''", "'''"});
		extention.put("python3", new String [] {".py", "'''", "'''"});
		extention.put("c", new String [] {".c", "/*", "*/"});
		extention.put("csharp", new String [] {".cs", "/*", "*/"});
		extention.put("javascript", new String [] {".js", "/*", "*/"});
		extention.put("ruby", new String [] {".ruby", "=begin", "=end"});
		extention.put("swift", new String [] {".swift", "/*", "*/"});
		extention.put("golang", new String [] {".go", "/*", "*/"});
		extention.put("scala", new String [] {".scala", "/*", "*/"});
		extention.put("kotlin",new String [] {".kt", "/*", "*/"});
		extention.put("rust", new String [] {".rs", "/*", "*/"});
		extention.put("mysql", new String [] {".sql", "/*", "*/"});

		new File(fileLocation + "/exercises/").mkdirs();

		for(String sub: submissions) {
			driver.get(sub);
			Thread.sleep(2000);
			List<String> code = new LinkedList<>();
			List<WebElement> codeTmp = driver.findElements(By.className("ace_line"));
			for(WebElement c: codeTmp) {
				if(c != null) {
					code.add(c.getText());
					code.add("\n");
				}
			}
			String subExt = driver.findElements(By.id("result_language")).get(0).getText();
			String ext = null;
			if (extention.containsKey(subExt))
				ext = extention.get(subExt)[0];
			else
				ext = "txt";
			
			//get question description and difficulty
			String questionName = driver.findElements(By.xpath("//h4")).get(0).getText(); 
			String questionLink = driver.findElements(By.xpath("//h4/a")).get(0).getAttribute("href"); 
			System.out.println(questionLink);
			driver.get(questionLink);
			Thread.sleep(2000);
		
			String questionNameWithNumber = driver.findElements(By.className("css-v3d350")).get(0).getText(); 
			questionNameWithNumber = questionNameWithNumber.replaceAll("[^\\d]", "");
			String questionDiff = driver.findElements(By.xpath("//div[@class='css-10o4wqw']/div[1]")).get(0).getText();
			String questionNum = String.format("%05d", Integer.parseInt(questionNameWithNumber));
			questionName = questionName.replaceAll("\\s","");
			questionName = questionNum + "_" + questionName + "_" +  questionDiff;
			
			String questionDescription = new String();
			if (extention.containsKey(subExt)) {
				questionDescription += extention.get(subExt)[1];
				questionDescription += driver.findElement(By.className("question-content__JfgR")).getText();
				questionDescription += extention.get(subExt)[2];
			}
			else
				questionDescription += driver.findElement(By.className("question-content__JfgR")).getText();
			
			
			PrintWriter fileOut = null;
			try {
				fileOut = new PrintWriter(fileLocation + "/exercises/" + questionName + ext);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			fileOut.append(questionDescription);
			fileOut.append("\n \n");
			
			for(String c: code) {
				fileOut.append(c);
			}
			
			
			fileOut.close();
		}
		System.out.println("All done!");
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		//Specify the location where the files will be stored
		String location = "";
		//Your leetcode username
		String username = "";
		//Your leetcode password
		String password = "";
		//specify the location of your chromedriver.exe
		String chromeDiverLocation = "";

		
		System.setProperty("webdriver.chrome.driver", chromeDiverLocation);
		ChromeOptions options = new ChromeOptions();
		//uncomment this option if you want to speed things up (will not load the browser window)
		//options.addArguments("--headless");
		ChromeDriver driver = new ChromeDriver(options);
		
		login(driver, username, password);
		List<String> submissions = getExerciseLinks(location, driver);
		getExercises(location, driver, submissions);
	}
}
