package edu.pku.sei.conditon.dedu.writer;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import edu.pku.sei.conditon.auxiliary.ASTLocator;
import edu.pku.sei.conditon.dedu.DeduConditionVisitor;
import edu.pku.sei.conditon.dedu.DeduMain;
import edu.pku.sei.conditon.dedu.feature.CondVector;
import edu.pku.sei.conditon.dedu.feature.Predicate;
import edu.pku.sei.conditon.dedu.feature.TreeVector;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig;
import edu.pku.sei.conditon.dedu.predall.ConditionConfig.PROCESSING_TYPE;
import edu.pku.sei.conditon.dedu.predall.PredAllExperiment;
import edu.pku.sei.conditon.ds.VariableInfo;
import edu.pku.sei.conditon.util.JavaFile;
import edu.pku.sei.conditon.util.TypeUtil;

public abstract class Writer {

	protected static final ConditionConfig CONFIG = ConditionConfig.getInstance();

	protected final static boolean OMITTING_TEST = true;// !DeduMain.processingDefects4J;

	protected final static int UPPER_POS_BOUND = 4;

	protected static final PROCESSING_TYPE missionType = ConditionConfig.getInstance().getProcessType();

	protected final static boolean SKIP = true;
	
	protected final static int SKIP_STEP = Integer.MAX_VALUE;

	public static final String del = "\t";

	public static final String NONE = "NIL";

	public static final String UNKNOWN_TYPE = DeduConditionVisitor.UNKNOWN_TYPE;

	protected String outFilePrefix;
	
	public Writer(String outFilePrefix) {
		this.outFilePrefix = outFilePrefix;
	}

	/**
	 * The core method
	 */
	public abstract void write();

	protected static boolean omit(TreeVector vec) {
		// OMIT
		if (vec.getContextFeature().getFileName().contains("Test")) {
			return true;
		}

		// just for pred all experiment
		if (PredAllExperiment.condIdTestSet != null && PredAllExperiment.condIdTestSet.contains(vec.getId())) {
			return true;
		}
		return false;
	}

	private static final Set<String> nonD4jOmitLiteral = new HashSet<>();
	static {
		nonD4jOmitLiteral.add("$");
		nonD4jOmitLiteral.add("!$");
		nonD4jOmitLiteral.add("$ == null");
		nonD4jOmitLiteral.add("$ != null");
		nonD4jOmitLiteral.add("$ && !$");
		nonD4jOmitLiteral.add("$ && $");
		nonD4jOmitLiteral.add("$ || !$");
		nonD4jOmitLiteral.add("$ || $");
	}

	/**
	 * @param vec
	 * @return <code>true</code> if this vector needs to be removed
	 */
	protected static boolean omit(CondVector vec) {
		// OMIT
		if (OMITTING_TEST && vec.getContextFeature().getFileName().contains("Test")) {
			return true;
		}

		Predicate pred = vec.getPredicate();
		String literal = pred.getLiteral();

		if (missionType == PROCESSING_TYPE.D4J) {
			if (pred.getSlopNum() > UPPER_POS_BOUND) {
				return true;
			}
		} else {
			if (pred.getSlopNum() > UPPER_POS_BOUND && !(literal.contains(" || ") || literal.contains(" && "))) {
				return true;
			}

			if (pred.getSlopTypes().contains(UNKNOWN_TYPE)) {
				return true;
			}
		}

		if (literal.contains(" 0x") || literal.contains(" 0X")) {
			return true;
		}

		if (literal.contains("++") || literal.contains("--") || literal.contains(">>>")) {
			return true;
		}

		if (literal.contains("+=") || literal.contains("-=") || literal.contains("*=") || literal.contains("/=")
				|| literal.contains(" % ")) {
			return true;
		}

		if (literal.contains("super.")) {
			return true;
		}

		Expression expr = (Expression) JavaFile.genASTFromSource(pred.getOriLiteral(), ASTParser.K_EXPRESSION,
				DeduMain.JAVA_VERSION);
		if (expr instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) expr;
			if (mi.getExpression() != null && mi.getExpression() instanceof StringLiteral) {
				return true;
			}
		} else if (expr instanceof InfixExpression) {
			InfixExpression infix = (InfixExpression) expr;
			if (infix.getLeftOperand() instanceof NumberLiteral) {
				return true;
			}
			if (infix.getLeftOperand() instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) infix.getLeftOperand();
				if (mi.getExpression() instanceof QualifiedName || mi.getExpression() instanceof StringLiteral) {
					return true;
				}
			}

			if (ASTLocator.maybeConstant(infix.getLeftOperand().toString())) {
				return true;
			}

			if (ASTLocator.maybeConstant(infix.getRightOperand().toString())
					&& infix.getRightOperand() instanceof SimpleName) {
				return true;
			}
		}

		// if is processing other projects, remove project-related predicates, only
		// reserve primitive types and java lang types
		if (missionType == PROCESSING_TYPE.GIT_REPOS) {

			if (literal.endsWith(".class") || literal.startsWith("this.")) {
				return true;
			}

			if (nonD4jOmitLiteral.contains(literal)) {
				return true;
			}

			if (literal.contains("System.getProperty")) {
				return true;
			}

			if (literal.contains("$.equals(")) {
				return true;
			}

			if (literal.contains("\\u") || literal.contains("0X") || literal.contains("0x") || literal.contains("'")) {
				return true;
			}

			if (literal.contains("?") && literal.contains(":")) {
				return true;
			}

			if (literal.contains("[") && literal.contains("]")) {
				return true;
			}

			// in case of instanceof predicates
			String inst = "$ instanceof ";
			if (literal.contains(inst)) {

				if (pred.getSlopNum() != 1) {
					return true;
				}

				String insttype = literal.substring(inst.length()).trim();
				if (!TypeUtil.isJavaLangOrJavaUtilType(insttype)) {
					return true;
				}

			}

			boolean nonVar = true;
			// check the used vars are simple type
			for (VariableInfo info : vec.getLocals()) {
				// only process vars in the predicate
				if (info.getPositionFeatures().size() == 0) {
					continue;
				}

				nonVar = false;
				String type = TypeUtil.removeGenericType(info.getType());
				if ("boolean".equals(type) || "Boolean".equals(type) || "Class".equals(type) || "Throwable".equals(type)
						|| "Enum".equals(type) || "Object".equals(type) || "ClassLoader".equals(type)) {
					return true;
				}

				if (!(TypeUtil.isPrimitiveType(info.getType()) || TypeUtil.isJavaLangOrJavaUtilType(type))) {
					return true;
				}
			}

			if (nonVar) {
				return true;
			}

			for (String var : pred.getOriSlopVars()) {
				if (var.equals("this")) {
					return true;
				}
			}

			// check form
			if (usedProjectLimitedName(expr)) {
				return true;
			}

		}

		// just for pred all experiment
		if (CONFIG.isPredAllPreparing()) {
			if (PredAllExperiment.condIdTestSet != null && PredAllExperiment.condIdTestSet.contains(vec.getId())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param expr
	 * @return
	 */
	private static boolean usedProjectLimitedName(Expression expr) {

		class QNameVIsitor extends ASTVisitor {
			boolean used = false;

			@Override
			public boolean visit(InfixExpression node) {
				Expression right = node.getRightOperand();
				if (right instanceof SimpleName) {
					if (ASTLocator.maybeConstant(right.toString())) {
						used = true;
					}
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(QualifiedName node) {
				// only process the foremost qualifier
				if (node.getQualifier() instanceof QualifiedName) {
					return false;
				}
				String qua = node.getQualifier().toString();
				if (!TypeUtil.isJavaLangOrJavaUtilType(qua)) {
					used = true;
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(MethodInvocation node) {
				Expression expr = node.getExpression();
				if (expr == null) {
					used = true;
					return super.visit(node);
				}

				if (expr instanceof SimpleName == false) {
					used = true;
					return super.visit(node);
				}
				String caller = expr.toString();
				if (ASTLocator.maybeConstant(caller)) {
					used = true;
				} else if (Character.isUpperCase(caller.charAt(0)) && !TypeUtil.isJavaLangOrJavaUtilType(caller)) {
					used = true;
				}

				return super.visit(node);
			}

		}
		;

		QNameVIsitor visitor = new QNameVIsitor();
		expr.accept(visitor);
		return visitor.used;
	}
	
	protected static Pattern pattern = Pattern.compile("[\\s\\t]+");
	public static String predForCSV(String expr) {
		String rightExpr = expr.replace('\n', ' ');
		Matcher matcher = pattern.matcher(rightExpr);
		String exprForCsv = matcher.replaceAll(" ");
		// exprForCsv = ExprNormalization.normalize(exprForCsv, info.getType());
		return exprForCsv;
	}
	
	public static String predForCSV(Predicate pred){
		String expr = pred.getLiteral();
		String rightExpr = expr.replace('\n', ' ');
		Matcher matcher = pattern.matcher(rightExpr);
		String exprForCsv = matcher.replaceAll(" ");
		return exprForCsv;
	}


}
