package cn.edu.pku.sei.plde.hanabi.trace.collector;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.pku.sei.plde.hanabi.trace.MethodTestRunner;
import cn.edu.pku.sei.plde.hanabi.trace.runtime.RuntimeValues;
import cn.edu.pku.sei.plde.hanabi.utils.FileUtil;
import cn.edu.pku.sei.plde.hanabi.utils.Pair;

public abstract class ValueCollector {
	
	private static Collection<ValueCollector> classCollectors;
    private static Collection<ValueCollector> primitiveCollectors;
	
	protected abstract Class<?> collectingClass();
    protected abstract Collection<Pair<String, Object>> collectedValues(String name, Object value);
    
    public static void collectFrom(String name, Object value) {
        if (!collectWith(primitiveCollectors(), name, value)) {
            boolean isNotNull = value != null;
            if(name.equals("this")) {
            	// record fields be reflect
            	Field[] fields = value.getClass().getDeclaredFields();
            	for(Field field : fields) {
            		int fldMod = field.getModifiers();
            		if(Modifier.isStatic(fldMod)) {
            			continue;
            		}
    				
            		String fieldType = field.getType().getName();
            		if(!isPrimitiveType(fieldType)) {
            			continue;
            		}
            		String recordName = "this." + field.getName();
            		
					boolean accessible = field.isAccessible();
					if(accessible) {
						continue;
					}else {
						field.setAccessible(true);
					}
            		try {
						Object fldValue = field.get(value);						
	                	collectWith(primitiveCollectors(), recordName, fldValue);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						
					} finally {
						if(!accessible) {//reset accessible
							field.setAccessible(accessible);
						}
					}
            	}
            	
            }else {
            	Pair<String, Object> pair = Pair.from(name + "!=null", (Object) new Boolean(isNotNull));
            	RuntimeValues.putInStorage(pair);

            }
            if (isNotNull) {
                collectWith(getClassCollectors(), name, value);
                collectObjectType(name, value);
            }
        }
    }
    
    private static Collection<ValueCollector> primitiveCollectors() {
        if (primitiveCollectors == null) {
            Collection<ValueCollector> collectorInstances = new ArrayList<>();
            collectorInstances.add(new BooleanCollector());
            collectorInstances.add(new NumberCollector());
            collectorInstances.add(new CharacterCollector());
            primitiveCollectors = collectorInstances;
        }
        return primitiveCollectors;
    }

    private static void collectObjectType(String name, Object value){
    	List<String> allType = new ArrayList<>();
    	allType.add(value.getClass().getName());
    	for(Class interFace : value.getClass().getInterfaces()){
    		allType.add(interFace.getName());
    	}
    	for(String type : allType){
        	//if(type.startsWith("java.")){//only collect jdk's object type
    		if(type.equals("java.lang.Object") || type.equals("java.io.Serializable")) {
    			continue;
    		}
            //Pair<String, Object> pair = Pair.from(name + " instanceof " + type, (Object) new Boolean(true));
        	//RuntimeValues.putInStorage(pair);
        	//}
    	}
    }
    
    private static boolean collectWith(Collection<ValueCollector> collectors, String name, Object value) {
        for (ValueCollector collector : collectors) {
            if (collector.handlesClassOf(value)) {
                Collection<Pair<String, Object>> collected = collector.collectedValues(name, value);
                for (Pair<String, Object> collectedPair : collected) {
                	RuntimeValues.putInStorage(collectedPair);
                }
                return true;
            }
        }
        return false;
    }
    public boolean handlesClassOf(Object value) {
        return collectingClass().isInstance(value);
    }
    
    private static Collection<ValueCollector> getClassCollectors() {
        if (classCollectors == null) {
            Collection<ValueCollector> collectorInstances = new ArrayList<>();
            collectorInstances.add(new ArrayCollector());
            collectorInstances.add(new CollectionCollector());
            collectorInstances.add(new CharSequenceCollector());
            collectorInstances.add(new DictionaryCollector());
            collectorInstances.add(new MapCollector());
            collectorInstances.add(new IteratorCollector());
            collectorInstances.add(new EnumerationCollector());
            classCollectors = collectorInstances;
        }
        return classCollectors;
    }
    
    private static boolean isPrimitiveType(String type){
		switch (type) {
		case "int":
		case "boolean":
		case "char":
		case "double":
		case "short":
		case "long":
		case "byte":
		case "float":
			return true;
		default:
			return false;
		}
	}

}
