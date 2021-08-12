package cn.edu.pku.sei.plde.hanabi.external;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.pku.sei.plde.hanabi.build.testrunner.BugsDotJarTestRunner;
import cn.edu.pku.sei.plde.hanabi.main.Config;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.GitUtil;
import cn.edu.pku.sei.plde.hanabi.utils.JavaCodeUtil;

/**
 * Important class, defines flasky tests
 * @author Wang Bo
 *
 */
public class BugsDotJarTestAnalyser {

	private static final String INFO_FOLDER = "./bugs-dot-jar-info/";

	private static final String BRANCH_FILE_TAIL = "_branches.txt";

	/**
	 * Map: branch name -> flaky test in the original report of bus.jar
	 */
	private static final Map<String, String> CHECKED_TESTS = new HashMap<>();
	
	static {
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-3690_2a3f3392", 
				"org.apache.camel.impl.CamelContextAddRouteDefinitionsFromXmlTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-5704_708e756d", 
				"org.apache.camel.processor.aggregator.AggregateTimeoutWithExecutorServiceTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-5570_a57830ed", 
				"org.apache.camel.component.file.AntPathMatcherGenericFileFilterTest" + ":" +
				"org.apache.camel.processor.aggregator.AggregateTimeoutTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-5707_3f70d612", 
				"org.apache.camel.processor.aggregator.AggregateTimeoutWithExecutorServiceTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-6987_37e0e6bb", 
				"org.apache.camel.processor.aggregator.AggregateTimeoutTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-7241_18c23fa8", 
				"org.apache.camel.processor.aggregator.AggregateTimeoutTest" + ":" + 
				"org.apache.camel.component.file.FileConsumerIdempotentTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-7344_91228815", 
				"org.apache.camel.management.ManagedThrottlerTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-7459_57ba1bde", 
				"org.apache.camel.management.ManagedThrottlerTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-7448_35bde2b2", 
				"org.apache.camel.issues.OnCompletionIssueTest");
		
		CHECKED_TESTS.put("bugs-dot-jar_CAMEL-7611_e30f1c53", 
				"org.apache.camel.management.ManagedThrottlerTest");
	}
	
	public static void main(String[] args) {
		String proj = "camel";

		// first generate test file
		// generateTest(proj);

		// analysis
		analysis(proj);
	}

	private static void generateTest(String proj) {
		File info = new File(INFO_FOLDER + proj + BRANCH_FILE_TAIL);
		List<String> bugs = FileUtil.readFileToStringList(info);

		StringWriter writer = new StringWriter();

		int idx = "bugs-dot-jar_".length();

		final String indent = "\t";
		
		int total = 0;
		for (String bug : bugs) {
			if(bug.startsWith("#")) {
				continue;
			}
			
			String tail = bug.substring(idx);
			tail = tail.replaceAll("\\-", "_");

			writer.write(indent + "@Test(timeout=TIME_OUT)\n");
			writer.write(indent + "public void test_" + tail + "(){\n");

			// TODO:
			// writer.write(indent + indent + "assertTrue\n");
			writer.write(indent + "}\n\n");
			
			total++;
		}

		String result = writer.toString();
		System.out.println(result);
		
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("TOTOAL NUM: " + total);
	}

	private static void analysis(String proj) {
		File info = new File(INFO_FOLDER + proj + BRANCH_FILE_TAIL);
		List<String> bugs = FileUtil.readFileToStringList(info);

		for (String bug : bugs) {
			if(bug.startsWith("#") || bug.trim().length() == 0) {
				continue;
			}
			processBug(proj, bug);
		}

	}

	private static void processBug(String proj, String bugBranch) {
		
		// First clean the git repo
		String projRoot = Config.BUGS_DOT_JAR_ROOT + proj + "/";
		File projRootFile = new File(projRoot);

		assert projRootFile.exists();
		
		GitUtil.resetAndCleanRepo(projRootFile);
		GitUtil.gitCheckout(bugBranch, projRootFile, false);
		
		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>  " + bugBranch + "  >>>>>>>>>>>>>>>>>>>>>>>>>>");
		
		File resFile = new File(projRoot + ".bugs-dot-jar/test-results.txt");
		assert resFile.exists(): resFile.getAbsolutePath();
		Set<String> summaryfailedList = analysisTestOutput(bugBranch, resFile);
		
		String patchedSrc = anlysisPatch(projRoot + ".bugs-dot-jar/developer-patch.diff");
		
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");	
		
		for(String str : summaryfailedList) {
			String common = JavaCodeUtil.getLongestCommonPackge(str, patchedSrc);
			
			// patch and failure tests are in different packages 
			if(common.endsWith(proj)) {
				System.out.println("DIFF  " + bugBranch);
			}
		}
		
	}

	public static Set<String> analysisTestOutput(String bugBranch, File testResFile) {
		List<String> lines = FileUtil.readFileToStringList(testResFile);
		
		boolean beginSummary = false;
		boolean beginFailTest = false;
		int failureNum = 0;
		int errNum = 0;
		
		String preLine = "";
		
		LinkedHashSet<String> failedClassList = new LinkedHashSet<>();
		
		LinkedHashSet<String> summaryfailedList = new LinkedHashSet<>();
		
		boolean format1 = false;
		boolean format2 = false;
		
		for(String str: lines) {
			if(str.startsWith("Tests run: ") && str.contains("<<< FAILURE!")) {
				assert preLine.startsWith("Running ");
				failedClassList.add(preLine.split(" ")[1]);
			}
			
			if(!beginSummary) {
				if(str.equals("Results :")) {
					beginSummary = true;
				}
			} else {
				if(str.startsWith("Tests run: ")) {
					int[] resultNumbers = BugsDotJarTestRunner.processSummaryLine(str);
					failureNum = resultNumbers[1];
					errNum = resultNumbers[2];
				
				} else if(!beginFailTest) {
					if(str.trim().equals("Failed tests:") || str.trim().equals("Tests in error:")) {
						beginFailTest = true;
					}
					
				} else {
					String strTrim = str.trim();
					if(strTrim.contains("(") && strTrim.contains(")")) {
						// "testEndpointShutdown(org.apache.camel.impl.EndpointShutdownOnceTest)"
						String[] arr = strTrim.split("\\(");
						
						assert arr.length == 2;
						
						String mtd = arr[0];
						String cls = arr[1].split("\\)")[0];// remove ')'
						summaryfailedList.add(cls + "#" + mtd);
						
						format1 = true;
					}else if(strTrim.contains("->")) {
						// "CustomListAggregationStrategyEmptySplitTest>TestSupport.runBare:58->testCustomAggregationStrategy:44 » CamelExecution"
						// "XMLTokenExpressionIteratorInvalidXMLTest.testExtractToken:58->invokeAndVerify:73 the error expected"
						String shortName;
						if(strTrim.split("->")[0].contains(">")) {
							shortName = strTrim.split(">")[0];
						}else {
							shortName = strTrim.split("\\.")[0];
						}
						
						assert shortName.contains(".") == false;
						
						String clsName = "";
						for(String fullName : failedClassList) {
							if(fullName.endsWith("." + shortName)) {
								clsName = fullName;
							}
						}

						assert clsName.equals("") == false;
						
						String mtd = (strTrim.split("->")[1]).split(":")[0];
						summaryfailedList.add(clsName + "#" + mtd);
						
						format2 = true;
					} else {
						if(strTrim.contains(".")) {
							String shortName = strTrim.split("\\.")[0];
							if(JavaCodeUtil.isTestCase(shortName)) {
								
								String mtd = strTrim.split("\\.")[1];
								if(mtd.contains(":")) {
									mtd = mtd.split(":")[0];
								}
								String clsName = "";
								for(String fullName : failedClassList) {
									if(fullName.endsWith("." + shortName)) {
										clsName = fullName;
									}
								}
								assert clsName.equals("") == false;

								summaryfailedList.add(clsName + "#" + mtd);
							}
							
						}
					}
				}
			}
			
			if(str.equals("[INFO] BUILD FAILURE")) {
				break;
			}
			
			preLine = str;
		}// end for(String str: lines)
		
		assert summaryfailedList.isEmpty() == false;
		assert format1 ^ format2 == true;
		
		
		// remove flaky tests
		List<String> tobeRemoved = new ArrayList<>();
		for(String res : summaryfailedList) {
			if(CHECKED_TESTS.containsKey(bugBranch)) {
				String cls = res.split("#")[0];
				String[] skipped = CHECKED_TESTS.get(bugBranch).split(":");
				
				for(String str : skipped) {
					if(cls.equals(str)) {
						tobeRemoved.add(res);
					}
				}
			}
		}
		summaryfailedList.removeAll(tobeRemoved);
				
		return summaryfailedList;
	}
	
	private static String anlysisPatch(String path) {
		String patchSrc = "";
		List<String> lines = FileUtil.readFileToStringList(path);
		for(String line : lines) {
			if(line.startsWith("---")) {
				patchSrc = line.split("/src/main/java/")[1];
				
				patchSrc = patchSrc.replaceAll("\\.java", "");
				
				patchSrc = patchSrc.replaceAll("/", "\\.");
				break;
			}
			
		}
		
		assert patchSrc.equals("") == false;
		System.out.println(">>>>>>>");
		System.out.println("    " + patchSrc);
		
		return patchSrc;
	}
	
}
