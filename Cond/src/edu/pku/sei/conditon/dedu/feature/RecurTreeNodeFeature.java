package edu.pku.sei.conditon.dedu.feature;

public class RecurTreeNodeFeature extends Feature {

	private static final long serialVersionUID = -1504533780661342701L;

	private String nodeType;
	private boolean isRoot;
	private String parentType;
	private String locationInParent;
	private int depth;

	public RecurTreeNodeFeature(String nodeType, boolean isRoot, String parentType, String locationInParent,
			int depth) {
		this.nodeType = nodeType;
		this.isRoot = isRoot;
		this.parentType = parentType;
		this.locationInParent = locationInParent;
		this.depth = depth;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getNodeType() {
		return nodeType;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public String getParentType() {
		return parentType;
	}

	public String getLocationInParent() {
		return locationInParent;
	}

	public int getDepth() {
		return depth;
	}

	private static String cache;

	public static String getFeatureHeader() {
		if (cache != null) {
			return cache;
		}
		cache = Feature.genFeatureHeaderFromList("recur");
		return cache;
	}
}
