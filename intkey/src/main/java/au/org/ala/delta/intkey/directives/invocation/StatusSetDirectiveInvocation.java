package au.org.ala.delta.intkey.directives.invocation;

import java.awt.Color;

import au.org.ala.delta.intkey.model.IntkeyContext;
import au.org.ala.delta.intkey.model.ReportUtils;
import au.org.ala.delta.intkey.ui.UIUtils;
import au.org.ala.delta.rtf.RTFBuilder;

public class StatusSetDirectiveInvocation extends IntkeyDirectiveInvocation {

    @Override
    public boolean execute(IntkeyContext context) throws IntkeyDirectiveInvocationException {
        RTFBuilder builder = new RTFBuilder();
        builder.startDocument();

        builder.setTextColor(Color.BLUE);
        builder.appendText(UIUtils.getResourceString("Status.Set.title"));
        builder.setTextColor(Color.BLACK);

        ReportUtils.generateStatusSetContent(context, builder);

        builder.endDocument();

        context.getUI().displayRTFReport(builder.toString(), UIUtils.getResourceString("Status.title"));

        return true;
    }

}