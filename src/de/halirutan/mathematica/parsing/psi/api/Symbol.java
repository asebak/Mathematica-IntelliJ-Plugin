package de.halirutan.mathematica.parsing.psi.api;

import com.intellij.psi.PsiNamedElement;

/**
 * Created with IntelliJ IDEA.
 * User: patrick
 * Date: 3/28/13
 * Time: 12:33 AM
 * Purpose:
 */
public interface Symbol extends PsiNamedElement {
    String getMathematicaContext();

    String getSymbolName();
}
