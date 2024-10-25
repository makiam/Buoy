package buoy.xml.delegate;

import buoy.widget.*;
import java.beans.*;

/**
 * This class is a PersistenceDelegate for serializing BorderContainers.
 *
 * @author Peter Eastman
 */
public class BorderContainerDelegate extends EventSourceDelegate {

    /**
     * Create a BorderContainerDelegate.
     */

    public BorderContainerDelegate() {
    }

    @Override
    protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        BorderContainer oldC = (BorderContainer) oldInstance;
        BorderContainer newC = (BorderContainer) newInstance;
        BorderContainer.Position[] pos = new BorderContainer.Position[]{
            BorderContainer.CENTER, BorderContainer.NORTH, BorderContainer.SOUTH, BorderContainer.EAST, BorderContainer.WEST};
        for (BorderContainer.Position po : pos) {
            if(oldC.getChild(po) == newC.getChild(po)) continue;
            out.writeStatement(new Statement(oldInstance, "add", new Object[]{oldC.getChild(po), po, oldC.getChildLayout(po)}));
        }
    }
}
