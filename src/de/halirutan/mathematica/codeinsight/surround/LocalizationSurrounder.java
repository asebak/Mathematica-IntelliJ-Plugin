/*
 * Copyright (c) 2014 Patrick Scheibe
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.halirutan.mathematica.codeinsight.surround;

import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import de.halirutan.mathematica.filetypes.MathematicaFileType;
import de.halirutan.mathematica.parsing.psi.api.Expression;
import de.halirutan.mathematica.parsing.psi.api.FunctionCall;
import de.halirutan.mathematica.parsing.psi.util.MathematicaPsiUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author patrick (6/12/14)
 */
public class LocalizationSurrounder implements Surrounder {
  private String myHead;


  public LocalizationSurrounder(String head) {
    this.myHead = head;
  }

  @Override
  public String getTemplateDescription() {
    return myHead + "[{}, code]";
  }

  @Override
  public boolean isApplicable(@NotNull PsiElement[] elements) {
    return elements.length == 1 && elements[0] != null && elements[0] instanceof Expression;
  }

  @Nullable
  @Override
  public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement[] elements) throws IncorrectOperationException {
    assert (elements.length == 1 && elements[0] != null) || PsiTreeUtil.findCommonParent(elements) == elements[0].getParent();
    final PsiElement e = elements[0];
    CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

    final PsiFileFactory factory = PsiFileFactory.getInstance(project);
    final StringBuilder stringBuilder = new StringBuilder(myHead + "[{},\n");
    stringBuilder.append(e.getText());
    stringBuilder.append("\n]");

    final PsiFile file = factory.createFileFromText("dummy.m", MathematicaFileType.INSTANCE, stringBuilder);
    final FunctionCall[] func = PsiTreeUtil.getChildrenOfType(file, FunctionCall.class);
    assert func != null && func[0] != null;
    func[0] = (FunctionCall) codeStyleManager.reformat(func[0]);
    PsiElement newElement = e.replace(func[0]);

    final List<PsiElement> arguments = MathematicaPsiUtilities.getArguments(newElement);
    if (arguments.isEmpty()) {
      return null;
    }
    // We want to place the cursor inside the {} of Module[{}, code], therefore we use the first argument which is
    // {} and step one letter ahead.
    return TextRange.from(arguments.get(0).getTextOffset() + 1, 0);
  }
}
