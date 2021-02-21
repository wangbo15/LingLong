package edu.pku.sei.conditon.auxiliary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.pku.sei.conditon.util.JavaFile;

public final class DollarilizeVisitor extends ASTVisitor{
	
	private StringBuffer buffer = new StringBuffer();
	
	private List<String> variables = new ArrayList<>();
	
	private static final int JLS2 = AST.JLS2;
	
	private static final int JLS3 = AST.JLS3;

	private static final int JLS4 = AST.JLS4;
	
	/**
	 * @param str
	 * @return	list[0] is predicate, others are variables
	 */
	public static List<String> parse(String str){
		List<String> result = new ArrayList<>();
		try {
			Expression expr = (Expression) JavaFile.genASTFromSourceAsJava7(str, ASTParser.K_EXPRESSION);
			DollarilizeVisitor visitor = new DollarilizeVisitor();
			expr.accept(visitor);
			
			String pred = visitor.buffer.toString();
			result.add(pred.replaceAll("\n", " "));
			result.addAll(visitor.variables);
		} catch (ClassCastException e) {
			System.err.println(str);
			e.printStackTrace();
		}
		return result;
	}
	
	public static String dollarilize(Expression expr) {
		String result = DollarilizeVisitor.naiveDollarilize(expr);
		result = result.replaceAll("\n", " ");
		return result;
	}
	
	public static String dollarilize(String exprStr) {
		Expression expr = (Expression) JavaFile.genASTFromSourceAsJava7(exprStr, ASTParser.K_EXPRESSION);
		return dollarilize(expr);
	}
	
	public static String naiveDollarilize(Expression expr){
		DollarilizeVisitor visitor = new DollarilizeVisitor();
		expr.accept(visitor);
		return visitor.buffer.toString();
	}
	
	private void visitTypeAnnotations(AnnotatableType node) {
		if (node.getAST().apiLevel() >= AST.JLS8) {
			visitAnnotationsList(node.annotations());
		}
	}
	
	private void visitAnnotationsList(List annotations) {
		for (Iterator it = annotations.iterator(); it.hasNext(); ) {
			Annotation annotation = (Annotation) it.next();
			annotation.accept(this);
			this.buffer.append(' ');
		}
	}
	
	private void visitComponentType(ArrayType node) {
		node.getComponentType().accept(this);
	}
	
	void printModifiers(List ext) {
		for (Iterator it = ext.iterator(); it.hasNext(); ) {
			ASTNode p = (ASTNode) it.next();
			p.accept(this);
			this.buffer.append(" ");//$NON-NLS-1$
		}
	}
	
	void printModifiers(int modifiers) {
		if (Modifier.isPublic(modifiers)) {
			this.buffer.append("public ");//$NON-NLS-1$
		}
		if (Modifier.isProtected(modifiers)) {
			this.buffer.append("protected ");//$NON-NLS-1$
		}
		if (Modifier.isPrivate(modifiers)) {
			this.buffer.append("private ");//$NON-NLS-1$
		}
		if (Modifier.isStatic(modifiers)) {
			this.buffer.append("static ");//$NON-NLS-1$
		}
		if (Modifier.isAbstract(modifiers)) {
			this.buffer.append("abstract ");//$NON-NLS-1$
		}
		if (Modifier.isFinal(modifiers)) {
			this.buffer.append("final ");//$NON-NLS-1$
		}
		if (Modifier.isSynchronized(modifiers)) {
			this.buffer.append("synchronized ");//$NON-NLS-1$
		}
		if (Modifier.isVolatile(modifiers)) {
			this.buffer.append("volatile ");//$NON-NLS-1$
		}
		if (Modifier.isNative(modifiers)) {
			this.buffer.append("native ");//$NON-NLS-1$
		}
		if (Modifier.isStrictfp(modifiers)) {
			this.buffer.append("strictfp ");//$NON-NLS-1$
		}
		if (Modifier.isTransient(modifiers)) {
			this.buffer.append("transient ");//$NON-NLS-1$
		}
	}
	
	public boolean visit(Assignment node) {
		node.getLeftHandSide().accept(this);
		this.buffer.append(node.getOperator().toString());
		node.getRightHandSide().accept(this);
		return false;
	}
	
	public boolean visit(ArrayAccess node) {
		node.getArray().accept(this);
		this.buffer.append("[");//$NON-NLS-1$
		node.getIndex().accept(this);
		this.buffer.append("]");//$NON-NLS-1$
		return false;
	}
	
	public boolean visit(ArrayType node) {
		if (node.getAST().apiLevel() < AST.JLS8) {
			visitComponentType(node);
			this.buffer.append("[]");//$NON-NLS-1$
		} else {
			node.getElementType().accept(this);
			List dimensions = node.dimensions();
			int size = dimensions.size();
			for (int i = 0; i < size; i++) {
				Dimension aDimension = (Dimension) dimensions.get(i);
				aDimension.accept(this);
			}
		}
		return false;
	}
	
	public boolean visit(BooleanLiteral node) {
		if (node.booleanValue() == true) {
			this.buffer.append("true");//$NON-NLS-1$
		} else {
			this.buffer.append("false");//$NON-NLS-1$
		}
		return false;
	}

	public boolean visit(CharacterLiteral node) {
		this.buffer.append(node.getEscapedValue());
		return false;
	}
	
	public boolean visit(CastExpression node) {
		this.buffer.append("(");//$NON-NLS-1$
		node.getType().accept(this);
		this.buffer.append(")");//$NON-NLS-1$
		node.getExpression().accept(this);
		return false;
	}
	
	public boolean visit(ConditionalExpression node) {
		node.getExpression().accept(this);
		this.buffer.append(" ? ");//$NON-NLS-1$
		node.getThenExpression().accept(this);
		this.buffer.append(" : ");//$NON-NLS-1$
		node.getElseExpression().accept(this);
		return false;
	}
	
	public boolean visit(ClassInstanceCreation node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append("new ");//$NON-NLS-1$
		if (node.getAST().apiLevel() >= JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.buffer.append("<");//$NON-NLS-1$
				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(",");//$NON-NLS-1$
					}
				}
				this.buffer.append(">");//$NON-NLS-1$
			}
			node.getType().accept(this);
		}
		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(")");//$NON-NLS-1$
		if (node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration().accept(this);
		}
		return false;
	}
	
	public boolean visit(Dimension node) {
		List annotations = node.annotations();
		if (annotations.size() > 0)
			this.buffer.append(' ');
		visitAnnotationsList(annotations);
		this.buffer.append("[]"); //$NON-NLS-1$
		return false;
	}

	
	public boolean visit(FieldAccess node) {
		node.getExpression().accept(this);
		this.buffer.append(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}
	
	public boolean visit(InfixExpression node) {
		node.getLeftOperand().accept(this);
		this.buffer.append(' ');  // for cases like x= i - -1; or x= i++ + ++i;
		this.buffer.append(node.getOperator().toString());
		this.buffer.append(' ');
		node.getRightOperand().accept(this);
		final List extendedOperands = node.extendedOperands();
		if (extendedOperands.size() != 0) {
			this.buffer.append(' ');
			for (Iterator it = extendedOperands.iterator(); it.hasNext(); ) {
				this.buffer.append(node.getOperator().toString()).append(' ');
				Expression e = (Expression) it.next();
				e.accept(this);
			}
		}
		return false;
	}
	
	public boolean visit(InstanceofExpression node) {
		node.getLeftOperand().accept(this);
		this.buffer.append(" instanceof ");//$NON-NLS-1$
		node.getRightOperand().accept(this);
		return false;
	}
	
	public boolean visit(MethodInvocation node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		
		if (!node.typeArguments().isEmpty()) {
			this.buffer.append("<");//$NON-NLS-1$
			for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.buffer.append(",");//$NON-NLS-1$
				}
			}
			this.buffer.append(">");//$NON-NLS-1$
		}
		
		node.getName().accept(this);
		this.buffer.append("(");//$NON-NLS-1$;
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}
	
	public boolean visit(Modifier node) {
		this.buffer.append(node.getKeyword().toString());
		return false;
	}
	
	public boolean visit(NameQualifiedType node) {
		node.getQualifier().accept(this);
		this.buffer.append('.');
		visitTypeAnnotations(node);
		node.getName().accept(this);
		return false;
	}
	
	public boolean visit(NullLiteral node) {
		this.buffer.append("null");//$NON-NLS-1$
		return false;
	}
	
	public boolean visit(NumberLiteral node) {
		this.buffer.append(node.getToken());
		return false;
	}
	
	public boolean visit(ParenthesizedExpression node) {
		this.buffer.append("(");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}
	
	public boolean visit(PostfixExpression node) {
		node.getOperand().accept(this);
		this.buffer.append(node.getOperator().toString());
		return false;
	}

	public boolean visit(PrefixExpression node) {
		this.buffer.append(node.getOperator().toString());
		node.getOperand().accept(this);
		return false;
	}
	
	public boolean visit(PrimitiveType node) {
		visitTypeAnnotations(node);
		this.buffer.append(node.getPrimitiveTypeCode().toString());
		return false;
	}
	
	public boolean visit(QualifiedName node) {
		node.getQualifier().accept(this);
		this.buffer.append(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	public boolean visit(QualifiedType node) {
		node.getQualifier().accept(this);
		this.buffer.append(".");//$NON-NLS-1$
		visitTypeAnnotations(node);
		node.getName().accept(this);
		return false;
	}
	
	public boolean visit(SimpleName node) {
		if(Character.isUpperCase(node.getIdentifier().charAt(0))){
			this.buffer.append(node.getIdentifier());
			return false;
		}
		
		if(ASTLocator.notVarLocation(node) || ASTLocator.maybeConstant(node.getIdentifier())){
			this.buffer.append(node.getIdentifier());
		}else{
			this.buffer.append("$");
			this.variables.add(node.getIdentifier());
		}
		return false;
	}
	
	public boolean visit(SingleVariableDeclaration node) {
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		if (node.getAST().apiLevel() >= JLS3) {
			if (node.isVarargs()) {
				this.buffer.append("...");//$NON-NLS-1$
			}
		}
		this.buffer.append(" ");//$NON-NLS-1$
		node.getName().accept(this);
		int size = node.getExtraDimensions();
		if (node.getAST().apiLevel() >= AST.JLS8) {
			List dimensions = node.extraDimensions();
			for (int i = 0; i < size; i++) {
				visit((Dimension) dimensions.get(i));
			}
		} else {
			for (int i = 0; i < size; i++) {
				this.buffer.append("[]"); //$NON-NLS-1$
			}
		}
		if (node.getInitializer() != null) {
			this.buffer.append("=");//$NON-NLS-1$
			node.getInitializer().accept(this);
		}
		return false;
	}
	
	public boolean visit(SimpleType node) {
		visitTypeAnnotations(node);
		node.getName().accept(this);
		return false;
	}
	
	public boolean visit(StringLiteral node) {
		this.buffer.append(node.getEscapedValue());
		return false;
	}
	
	public boolean visit(SuperMethodInvocation node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append("super.");//$NON-NLS-1$
		node.getName().accept(this);
		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}
	
	public boolean visit(ThisExpression node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append("$");//$NON-NLS-1$
		this.variables.add("this");
		return false;
	}
	
	public boolean visit(TypeLiteral node) {
		node.getType().accept(this);
		this.buffer.append(".class");//$NON-NLS-1$
		return false;
	}
	
	public boolean visit(TypeParameter node) {
		
		node.getName().accept(this);
		if (!node.typeBounds().isEmpty()) {
			this.buffer.append(" extends ");//$NON-NLS-1$
			for (Iterator it = node.typeBounds().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.buffer.append(" & ");//$NON-NLS-1$
				}
			}
		}
		return false;
	}
	
	public boolean visit(VariableDeclarationFragment node) {
		node.getName().accept(this);
		int size = node.getExtraDimensions();
		if (node.getAST().apiLevel() >= AST.JLS8) {
			List dimensions = node.extraDimensions();
			for (int i = 0; i < size; i++) {
				visit((Dimension) dimensions.get(i));
			}
		} else {
			for (int i = 0; i < size; i++) {
				this.buffer.append("[]");//$NON-NLS-1$
			}
		}
		if (node.getInitializer() != null) {
			this.buffer.append("=");//$NON-NLS-1$
			node.getInitializer().accept(this);
		}
		return false;
	}
}
