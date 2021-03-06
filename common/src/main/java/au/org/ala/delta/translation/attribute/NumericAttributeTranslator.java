/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.delta.translation.attribute;

import au.org.ala.delta.model.CharacterType;
import au.org.ala.delta.model.NumericRange;
import au.org.ala.delta.model.attribute.AttributeChunkFormatter;
import au.org.ala.delta.model.attribute.DecimalPlacesAttributeChunkFormatter;
import au.org.ala.delta.model.attribute.DefaultAttributeChunkFormatter;
import au.org.ala.delta.model.attribute.SignificantFiguresAttributeChunkFormatter;
import au.org.ala.delta.model.impl.DefaultAttributeData;
import org.apache.commons.lang.StringUtils;

import au.org.ala.delta.model.NumericCharacter;
import au.org.ala.delta.model.format.AttributeFormatter;
import au.org.ala.delta.model.format.CharacterFormatter;
import au.org.ala.delta.model.format.Formatter.AngleBracketHandlingMode;
import au.org.ala.delta.model.format.Formatter.CommentStrippingMode;
import au.org.ala.delta.translation.ItemListTypeSetter;
import au.org.ala.delta.translation.attribute.CommentedValueList.Values;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

/**
 * The NumericAttributeTranslator is responsible for translating NumericCharacter attributes into 
 * natural language.
 */
public class NumericAttributeTranslator extends AttributeTranslator {

	/** The character associated with attribute to translate */
	private NumericCharacter<?> _character;
	
	/** Knows how to format character units  */
	private CharacterFormatter _formatter;
	
	/** Knows how to type set a range symbol */
	private ItemListTypeSetter _typeSetter;
	
	/** Omit the space in between a numeric attribute and the units */
	private boolean _omitSpaceBeforeUnits;
	
	/** True if OMIT LOWER FOR CHARACTERS was specified for the Character 
	 * to be translated */
	private boolean _omitLower;

    /** The number of decimal places to use when formatting attributes */
    private Integer _decimalPlaces;
	
	public NumericAttributeTranslator(
			NumericCharacter<?> character, 
			ItemListTypeSetter typeSetter, 
			AttributeFormatter formatter, 
			boolean omitSpaceBeforeUnits,
			boolean omitOr,
			boolean omitLower,
            Integer decimalPlaces) {
		super(formatter, omitOr);
		_character = character;
		_formatter = new CharacterFormatter(false, CommentStrippingMode.STRIP_ALL, AngleBracketHandlingMode.RETAIN, true, false);
		_typeSetter = typeSetter;
		_omitSpaceBeforeUnits = omitSpaceBeforeUnits;
		_omitLower = omitLower;
        _decimalPlaces = decimalPlaces;
	}
	
	@Override
	public String translateValue(String value) {
        DefaultAttributeData attributeData = new DefaultAttributeData(_character);
        DefaultAttributeChunkFormatter formatter = createAttributeChunkFormatter();
        try {
            attributeData.setValueFromString(value);

            if (_omitLower) {

                List<NumericRange> number = attributeData.getNumericValue();
                if (number.size() == 0) {
                    return "";
                }
                NumericRange range = number.get(0);


                StringBuilder result = new StringBuilder(formatter.formatNumber((BigDecimal) range.getNormalRange().getMaximumNumber()));
                if (range.hasExtremeHigh()) {
                    result.append("(").append(_typeSetter.rangeSeparator()).append(formatter.formatNumber((BigDecimal) range.getExtremeHigh())).append(")");
                }
                return result.toString();

            }


            return attributeData.parsedAttribute().getAsText(formatter);

        }
        catch (ParseException e) {
            // This shouldn't happen as the attribute has already been parsed at this point.
        }



		return value;
	}

    private DefaultAttributeChunkFormatter createAttributeChunkFormatter() {

        DefaultAttributeChunkFormatter formatter;
        String rangeSeparator = _typeSetter != null ? _typeSetter.rangeSeparator() : "-";
        if (_character.getCharacterType() == CharacterType.IntegerNumeric) {
            formatter = new DefaultAttributeChunkFormatter(false, rangeSeparator);
        }
        else {
            if (_decimalPlaces == null) {
                formatter = new SignificantFiguresAttributeChunkFormatter(false, rangeSeparator);
            }
            else {
                formatter = new DecimalPlacesAttributeChunkFormatter(false, rangeSeparator, _decimalPlaces);
            }
        }
        return formatter;
    }

	@Override
	public String rangeSeparator() {
		return _typeSetter.rangeSeparator();
	}
	

	/**
	 * Overrides the parent method to append the characters units, if any, to the translation.
	 */
	@Override
	public String translateValues(Values values) {
		
		StringBuilder output = new StringBuilder();
		
		String value = super.translateValues(values);
		if (StringUtils.isNotEmpty(value)) {
			output.append(value).append(getUnits());
		}
		return output.toString();
	}
	
	
	private String getUnits() {
		StringBuilder output = new StringBuilder();
		if (_character.hasUnits()) {
			String units = _formatter.formatUnits(_character);
			if (!_omitSpaceBeforeUnits) {
				output.append(" ");
			}
			output.append(units);
		}
		return output.toString();
	}

}
