
package Triangle.AbstractSyntaxTrees;

/**
 *
 * @author samuel
 */
public class FunctionArgument {
    public String name;
    public int type;
    // 0: val
    // 1: ptr

    public FunctionArgument(String name, int type) {
        this.name = name;
        this.type = type;
    }
     
    
    public String getType(){
        return switch(type){
            case 0 -> "i32";
            case 1 -> "ptr";
            default -> "i32";
        };
    }
}
