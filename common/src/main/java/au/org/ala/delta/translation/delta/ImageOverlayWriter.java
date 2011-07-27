package au.org.ala.delta.translation.delta;

import java.util.Formatter;
import java.util.List;

import au.org.ala.delta.model.Character;
import au.org.ala.delta.model.Illustratable;
import au.org.ala.delta.model.image.ImageOverlay;
import au.org.ala.delta.model.image.OverlayLocation;
import au.org.ala.delta.model.image.OverlayLocation.OLDrawType;
import au.org.ala.delta.model.image.OverlayType;
import au.org.ala.delta.util.Utils;

/**
 * Base class for exporters of directives that work with images and image
 * overlays.
 */
public class ImageOverlayWriter {

	private DeltaWriter _deltaWriter;
	public ImageOverlayWriter(DeltaWriter writer) {
		_deltaWriter = writer;
	}
	
	
	/**
	 * Outputs the supplied List of ImageOverlays to the Printer provided by the
	 * state.
	 * 
	 * @param overlayList
	 *            the overlays to output.
	 * @param startIndent
	 *            the indent to use when outputting the first overlay.
	 * @param subject
	 *            the subject of the image overlays. May be null if the list
	 *            does not contain value or state overlays.
	 */
	public void writeOverlays(List<ImageOverlay> overlayList,
			int startIndent, Illustratable subject) {
		
		OverlayLocation olLoc;
		StringBuilder _textBuffer = new StringBuilder();
		for (ImageOverlay overlay : overlayList) {
			int indent = startIndent;
			int curType = overlay.type;

			_textBuffer.append('<');
			if (curType != OverlayType.OLCOMMENT) {
				_textBuffer.append('@');
				_textBuffer.append(OverlayType.keywordFromType(curType));
				_textBuffer.append(' ');
			}

			// These types have only simple text, with no location information
			if (curType == OverlayType.OLSUBJECT
					|| curType == OverlayType.OLSOUND
					|| curType == OverlayType.OLCOMMENT) {

				String text = overlay.overlayText;
				if (curType == OverlayType.OLCOMMENT || curType == OverlayType.OLSUBJECT) {
					text = Utils.despaceRtf(text, true);
				}
				_textBuffer.append(text);
				_textBuffer.append('>');
				_deltaWriter.outputTextBuffer(_textBuffer.toString(), indent, indent + 5, true);
				_textBuffer = new StringBuilder();
				continue;
			}

			// All remaining types MUST have a location
			if (overlay.location.size() == 0)
				throw new RuntimeException("TDirInOutEx(ED_INTERNAL_ERROR)");
			olLoc = overlay.location.get(0);

			// Keyword, value, and state have arguments before the positioning
			// information
			if (curType == OverlayType.OLKEYWORD) {
				_textBuffer.append('\"');
				_textBuffer.append(overlay.keywords);
				_textBuffer.append('\"');
				_textBuffer.append(' ');
			} else if (curType == OverlayType.OLVALUE) {
				Character character = (Character) subject;
				if (!character.getCharacterType().isNumeric())
					throw new RuntimeException("TDirInOutEx(ED_INTERNAL_ERROR)");

				_textBuffer.append(overlay.getValueString());
				_textBuffer.append(' ');
			} else if (curType == OverlayType.OLSTATE) {
				Character character = (Character) subject;
				if (!character.getCharacterType().isMultistate())
					throw new RuntimeException("ED_INTERNAL_ERROR)");

				if (overlay.stateId <= 0)
					throw new RuntimeException("ED_INTERNAL_ERROR)");
				_textBuffer.append(overlay.stateId);
				_textBuffer.append(' ');
			}

			int xIndent = startIndent + _textBuffer.length();

			if (overlay.comment.length() > 0) {
				_textBuffer.append('<');

				_textBuffer.append(Utils.despaceRtf(overlay.comment, true));
				_textBuffer.append("> ");
			}

			_textBuffer.append("x=");
			if (curType == OverlayType.OLUNITS && olLoc.X == Short.MIN_VALUE)
				_textBuffer.append('~');
			else
				_textBuffer.append(olLoc.X);
			_textBuffer.append(" y=");
			if (curType == OverlayType.OLUNITS && olLoc.Y == Short.MIN_VALUE)
				_textBuffer.append('~');
			else
				_textBuffer.append(olLoc.Y);
			// These button types have only x and y co-ordinates.
			if (curType == OverlayType.OLOK || curType == OverlayType.OLCANCEL
					|| curType == OverlayType.OLNOTES) {
				_textBuffer.append('>');
				_deltaWriter.outputTextBuffer(_textBuffer.toString(), indent, indent, true);
				_textBuffer = new StringBuilder();
				continue;
			}

			if (curType != OverlayType.OLIMAGENOTES) {
				_textBuffer.append(" w=");
				_textBuffer.append(olLoc.W);
				_textBuffer.append(" h=");
				_textBuffer.append(olLoc.H);
			}

			// Output hotspot information
			int tmpIndent = indent;
			for (int loc = 1; loc<overlay.location.size(); loc++) {
				OverlayLocation hsLoc = overlay.location.get(loc);
				_deltaWriter.outputTextBuffer(_textBuffer.toString(), tmpIndent, tmpIndent, true);
				_textBuffer = new StringBuilder();
				tmpIndent = xIndent;

				_textBuffer.append(" x=").append(hsLoc.X);
				_textBuffer.append(" y=").append(hsLoc.Y);
				_textBuffer.append(" w=").append(hsLoc.W);
				_textBuffer.append(" h=").append(hsLoc.H);
				if (hsLoc.drawType == OLDrawType.ellipse)
					_textBuffer.append(" e");
				if (hsLoc.isPopup())
					_textBuffer.append(" p");
				if (hsLoc.isColorSet()) {
					_textBuffer.append(" f=");
					Formatter formatter = new Formatter(_textBuffer);
					formatter.format("%06X", hsLoc.getColorAsBGR());
				}
			}

			// Output other flags
			if (overlay.omitDescription())
				_textBuffer.append(" n");
			if (overlay.includeComments())
				_textBuffer.append(" c");
			if (overlay.centreText())
				_textBuffer.append(" m");

			boolean hasText = false;
			if (overlay.overlayText.length() > 0) {
				_deltaWriter.outputTextBuffer(_textBuffer.toString(), tmpIndent, indent, true);
				_textBuffer = new StringBuilder();
				_textBuffer.append(" t=");
				_textBuffer.append(Utils.despaceRtf(overlay.overlayText, true));
				hasText = true;
			}
			if (hasText) {
				tmpIndent = indent +1; 
			}
			_textBuffer.append('>');
			_deltaWriter.outputTextBuffer(_textBuffer.toString(), tmpIndent, indent, true);
			_textBuffer = new StringBuilder();
		}

	}
	
}