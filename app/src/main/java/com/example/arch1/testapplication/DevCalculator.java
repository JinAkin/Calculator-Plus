package com.example.arch1.testapplication;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Stack;
import java.util.regex.Pattern;

public class DevCalculator extends AppCompatActivity implements View.OnClickListener,
        TextWatcher {

    private String equ = "", tempResult = "";

    private Button b0, b1, badd, bsub, bdiv, bmul, open, close, bdel, bequal, bdev_switch;
    private Button b2, b3, b4, b5, b6, b7, b8, b9, band, bor, bnot, bxor;
    private View mainLayout;
    private TextView result;
    private EditText equation;
    private History history;
    private View view;
    private Animator anim;
    private AppPreferences preferences;
    private Toolbar toolbar;
    private int devState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        preferences = AppPreferences.getInstance(this);
        setTheme(preferences.getStringPreference(AppPreferences.APP_THEME));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dev_calculator);

        initialisation();

        //getting values from saved Instance, if any
        if (savedInstanceState != null) {
            equ = savedInstanceState.getString("equ");
        }

        //getting primary color of the theme
        TypedValue typedValue = new TypedValue();
        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int color = a.getColor(0, 0);
        a.recycle();

        //setting toolbar manually
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(color);

        //avoiding keyboard input
        equation.setShowSoftInputOnFocus(false);
        equation.setTextIsSelectable(false);
        equation.setLongClickable(false);

        //adding button long press listener
        bdel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!equation.getText().toString().equals(""))
                    animateClear(view);
                equ = "";
                equation.setText(equ);
                result.setText("");
                return true;
            }
        });

        //adding text change listener
        equation.addTextChangedListener(this);

        setKeypadForBinary();

    }

    private void initialisation() {
        //initailisations
        mainLayout = findViewById(R.id.mainLayout);
        equation = findViewById(R.id.et_display1);
        result = findViewById(R.id.tv_display);
        history = new History(this);
        view = findViewById(R.id.view2);
        toolbar = findViewById(R.id.toolbar);

        //operators
        badd = mainLayout.findViewById(R.id.add);
        bsub = mainLayout.findViewById(R.id.sub);
        bmul = mainLayout.findViewById(R.id.mul);
        bdiv = mainLayout.findViewById(R.id.div);
        band = mainLayout.findViewById(R.id.and);
        bor = mainLayout.findViewById(R.id.or);
        bnot = mainLayout.findViewById(R.id.not);
        bxor = mainLayout.findViewById(R.id.xor);

        //numbers
        b0 = mainLayout.findViewById(R.id.zero);
        b1 = mainLayout.findViewById(R.id.one);
        b2 = mainLayout.findViewById(R.id.two);
        b3 = mainLayout.findViewById(R.id.three);
        b4 = mainLayout.findViewById(R.id.four);
        b5 = mainLayout.findViewById(R.id.five);
        b6 = mainLayout.findViewById(R.id.six);
        b7 = mainLayout.findViewById(R.id.seven);
        b8 = mainLayout.findViewById(R.id.eight);
        b9 = mainLayout.findViewById(R.id.nine);

        //misc
        bequal = mainLayout.findViewById(R.id.equal);
        bdel = mainLayout.findViewById(R.id.del);
        open = mainLayout.findViewById(R.id.open);
        close = mainLayout.findViewById(R.id.close);
        bdev_switch = mainLayout.findViewById(R.id.dev_switch);

        b0.setOnClickListener(this);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);
        b4.setOnClickListener(this);
        b5.setOnClickListener(this);
        b6.setOnClickListener(this);
        b7.setOnClickListener(this);
        b8.setOnClickListener(this);
        b9.setOnClickListener(this);

        badd.setOnClickListener(this);
        bsub.setOnClickListener(this);
        bmul.setOnClickListener(this);
        bdiv.setOnClickListener(this);
        band.setOnClickListener(this);
        bor.setOnClickListener(this);
        bnot.setOnClickListener(this);
        bxor.setOnClickListener(this);

        bequal.setOnClickListener(this);
        bdel.setOnClickListener(this);
        open.setOnClickListener(this);
        close.setOnClickListener(this);
        bdev_switch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        char c;

        switch (id) {
            case R.id.add:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (c == ')') {
                        add("+");
                        break;
                    }
                    if (isOperator(c)) {
                        if (equ.length() == 1) {
                            break;
                        }
                        removeBackOperators();
                        add("+");
                        break;
                    }
                    if (isNumber(c)) {
                        add("+");
                    }
                    break;
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add("+");
                        break;
                    }
                }
                break;

            case R.id.sub:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (c == ')') {
                        add("-");
                        break;
                    }

                    if (isOperator(c)) {
                        if (equ.length() >= 2 && (isNumber(equ.charAt(equ.length() - 2)))) {
                            if (c == '-') {
                                removeBackOperators();
                                add("+");
                                break;
                            }
                            add("-");
                            break;
                        }
                        removeBackOperators();
                        add("-");
                        break;
                    }

                    if (isNumber(c)) {
                        add("-");
                        break;
                    }
                    break;
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add("-");
                        break;
                    } else {
                        add("-");
                    }
                }
                break;

            case R.id.mul:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (c == ')' || isNumber(c)) {
                        add("\u00d7");
                        break;
                    }
                    if (isOperator(c)) {
                        if (equ.length() == 1) {
                            break;
                        }
                        removeBackOperators();
                        add("\u00d7");
                        break;
                    }
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add("\u00d7");
                        break;
                    }
                }
                break;

            case R.id.div:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (c == ')' || isNumber(c)) {
                        add("÷");
                        break;
                    }
                    if (isOperator(c)) {
                        if (equ.length() == 1) {
                            break;
                        }
                        removeBackOperators();
                        add("÷");
                        break;
                    }
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add("÷");
                        break;
                    }
                }
                break;

            case R.id.equal:
                if (!isEquationEmpty()) {
                    String res = result.getText().toString().trim();
                    if (res.equals("") || isAnError(res)) {
                        result.setText(Evaluate.errMsg);
                        result.setTextColor(getResources().getColor(R.color.colorRed));
                        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
                        result.startAnimation(shake);
                        break;
                    }
                    if (!res.equals("")) {
                        String historyEqu = equ;
                        String historyVal = res;
                        history.addToHistory(historyEqu, historyVal, System.currentTimeMillis());
                        tempResult = res;
                        equ = "";
                        equation.setText(res);
                        result.setText("");
                    }
                }
                break;

            case R.id.del:
                if (!isEquationEmpty()) {
                    equ = equ.substring(0, equ.length() - 1);
                    equation.setText(equ);
                } else {
                    tempResult = "";
                    equation.setText("");
                }
                break;

            case R.id.one:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d71");
                    break;
                }
                add("1");
                break;

            case R.id.two:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d72");
                    break;
                }
                add("2");
                break;

            case R.id.three:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d73");
                    break;
                }
                add("3");
                break;

            case R.id.four:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d74");
                    break;
                }
                add("4");
                break;

            case R.id.five:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d75");
                    break;
                }
                add("5");
                break;

            case R.id.six:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d76");
                    break;
                }
                add("6");
                break;

            case R.id.seven:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d77");
                    break;
                }
                add("7");
                break;

            case R.id.eight:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d78");
                    break;
                }
                add("8");
                break;

            case R.id.nine:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d79");
                    break;
                }
                add("9");
                break;

            case R.id.zero:
                tempResult = "";
                if (!isEquationEmpty() && equ.charAt(equ.length() - 1) == ')') {
                    add("\u00d70");
                    break;
                }
                add("0");
                break;

            case R.id.open:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (isNumber(c) || c == ')') {
                        add("\u00d7(");
                        break;
                    }
                    if (c == '(' || isOperator(c)) {
                        add("(");
                        break;
                    }
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add("\u00d7(");
                        break;
                    }
                    add("(");
                }
                break;

            case R.id.close:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (isNumber(c) || c == ')') {
                        add(")");
                        break;
                    }
                    if (c == '(') {
                        equ = equ.substring(0, equ.length() - 1);
                        equation.setText(equ);
                        break;
                    }
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add(")");
                        break;
                    }
                }
                break;

            case R.id.dev_switch:
                if (devState == 0) {
                    devState = 1;
                    bdev_switch.setText("DEC");
                    setKeypadForDecimal();
                    equation.setText(getDecimalEquation(equ));
                } else if (devState == 1) {
                    devState = 0;
                    bdev_switch.setText("BIN");
                    setKeypadForBinary();
                    equation.setText(equ);
                }
                break;

            case R.id.and:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (c == ')' || isNumber(c)) {
                        add("&");
                        break;
                    }
                    if (isOperator(c)) {
                        if (equ.length() == 1) {
                            break;
                        }
                        removeBackOperators();
                        add("&");
                        break;
                    }
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add("&");
                        break;
                    }
                }
                break;

            case R.id.or:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (c == ')' || isNumber(c)) {
                        add("|");
                        break;
                    }
                    if (isOperator(c)) {
                        if (equ.length() == 1) {
                            break;
                        }
                        removeBackOperators();
                        add("|");
                        break;
                    }
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add("|");
                        break;
                    }
                }
                break;

            case R.id.not:
                if(!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (c == '(') {
                        add("!");
                        break;
                    }
                    if (c == ')') {
                        for(int i=equ.length()-1; i>=0; i--) {
                            if(equ.charAt(i) == '(') {
                                equ = equ.substring(0, i) + "!" + equ.substring(i, equ.length());
                                equation.setText(equ);
                                break;
                            }
                        }
                    }
                    if (isOperator(c)) {
                        if(c == '!') {
                            equ = equ.substring(0, equ.length()-1);
                            equation.setText(equ);
                            break;
                        }
                        if(equ.length() == 1){
                            equ = "";
                            add("!");
                            break;
                        }
                        add("!");
                        break;
                    }
                    if( isNumber(c)) {
                        for(int i= equ.length()-1; i>=0; i--) {
                            if(equ.charAt(i) == '('){
                                equ = equ.substring(0, i) + "!" + equ.substring(i, equ.length()) + ")";
                                equation.setText(equ);
                                break;
                            }

                            if(isOperator(equ.charAt(i))){
                                equ = equ.substring(0, i+1) + "!(" + equ.substring(i+1, equ.length()) + ")";
                                equation.setText(equ);
                                break;
                            }
                        }
                        equ = "!" + equ;
                        equation.setText(equ);
                        break;
                    }
                } else {
                    if (!tempResult.equals("")) {
                        equ = "!" +tempResult;
                        tempResult = "";
                        equation.setText(equ);
                        break;
                    }
                    add("!");
                }

            case R.id.xor:
                if (!isEquationEmpty()) {
                    c = equ.charAt(equ.length() - 1);
                    if (c == ')' || isNumber(c)) {
                        add("\u2295");
                        break;
                    }
                    if (isOperator(c)) {
                        if (equ.length() == 1) {
                            break;
                        }
                        removeBackOperators();
                        add("\u2295");
                        break;
                    }
                } else {
                    if (!tempResult.equals("")) {
                        equ = tempResult;
                        tempResult = "";
                        add("\u2295");
                        break;
                    }
                }
                break;

        }
    }

    private boolean isEquationEmpty() {
        String eq = equ;
        if (eq.equals(""))
            return true;
        return false;
    }

    private void add(String str) {
        if (isEquationEmpty()) {
            equ = "";
        }
        equ += str;
        equation.setText(equ);
    }

    private boolean isNumber(char c) {
        switch (c) {
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '0':
            case 'e':
            case '\u03c0':
                return true;
        }
        return false;
    }

    private void removeBackOperators() {
        if (!isEquationEmpty()) {
            char c = equ.charAt(equ.length() - 1);
            while (isOperator(c)) {
                equ = equ.substring(0, equ.length() - 1);
                if (equ.length() == 0)
                    break;
                c = equ.charAt(equ.length() - 1);
            }
        }
        equation.setText(equ);
    }

    private boolean isOperator(char c) {
        if (c == '+' ||
                c == '/' ||
                c == '*' ||
                c == '%' ||
                c == '÷' ||
                c == '\u00d7' ||
                c == '-' ||
                c == '&' ||
                c == '|' ||
                c == '!' ||
                c == '\u2295')
            return true;
        return false;
    }

    private boolean isAnError(String string) {
        if (string.equals("Invalid Expression") ||
                string.equals("Domain error") ||
                string.equals("Cannot divide by 0") ||
                string.equals("Number too large"))
            return true;
        return false;
    }

    private void animateClear(View viewRoot) {
        int cx = viewRoot.getRight();
        int cy = viewRoot.getBottom();
        int l = viewRoot.getHeight();
        int b = viewRoot.getWidth();
        int finalRadius = (int) Math.sqrt((l * l) + (b * b));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, finalRadius);
            viewRoot.setVisibility(View.VISIBLE);
            anim.setDuration(300);
            anim.addListener(listener);
            anim.start();
        }
    }

    private Animator.AnimatorListener listener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            view.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        //check if smart calculations is enabled
        Boolean enableSmartCalculation = preferences.getBooleanPreference(AppPreferences.APP_SMART_CALCULATIONS);

        if (equ.equals(""))
            return;

        if (Evaluate.balancedParenthesis(equ)) {
            result.setTextColor(getTextColor());

            if (devState == 0) {
                String equ1 = getDecimalEquation(equ);
                String res = Evaluate.calculateResult(equ1, false, DevCalculator.this);
                result.setText(Integer.toBinaryString(Integer.parseInt(res)));
            }
            else {
                String res = Evaluate.calculateResult(equ, false, DevCalculator.this);
                result.setText(res);
            }
        } else {

            //trying to balance equation coz it's a smart calculator
            String tempEqu = Evaluate.tryBalancingBrackets(equ);

            //if smart calculations is on and was able to balance the equation
            if (Evaluate.balancedParenthesis(tempEqu) && enableSmartCalculation) {
                result.setTextColor(getTextColor());
                if (devState == 0) {
                    String equ1 = getDecimalEquation(equ);
                    String res = Evaluate.calculateResult(equ1, false, DevCalculator.this);
                    result.setText(Integer.toBinaryString(Integer.parseInt(res)));
                }
                else {
                    String res = Evaluate.calculateResult(equ, false, DevCalculator.this);
                    result.setText(res);
                }
            } else {
                result.setText("");
                Evaluate.errMsg = "Invalid Expression";
            }
        }
    }

    private int getTextColor() {
        String theme = preferences.getStringPreference(AppPreferences.APP_THEME);

        if (theme.equals("default") || theme.equals("")) {
            return getResources().getColor(R.color.colorBlack);
        }
        return getResources().getColor(R.color.colorWhite);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        menu.findItem(R.id.dev).setTitle("Standard Calculator");
        menu.findItem(R.id.deg).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int resId = item.getItemId();
        Intent intent;

        switch (resId) {
            case R.id.settings:
                intent = new Intent(DevCalculator.this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.dev:
                intent = new Intent(DevCalculator.this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.tutorial:
                //startTutorial();
                break;

            case R.id.about:
                intent = new Intent(DevCalculator.this, AboutActivity.class);
                startActivity(intent);
                break;

            case R.id.history:
                intent = new Intent(DevCalculator.this, HistoryActivity.class);
                startActivity(intent);
                break;

            case R.id.history_icon:
                intent = new Intent(DevCalculator.this, HistoryActivity.class);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void setTheme(String themeName) {
        if (themeName.equals("green")) {

            setTheme(R.style.GreenAppTheme);

        } else if (themeName.equals("orange")) {

            setTheme(R.style.AppTheme);

        } else if (themeName.equals("blue")) {

            setTheme(R.style.BlueAppTheme);

        } else if (themeName.equals("red")) {

            setTheme(R.style.RedAppTheme);

        } else if (themeName.equals("lgreen")) {

            setTheme(R.style.LightGreenAppTheme);

        } else if (themeName.equals("pink")) {

            setTheme(R.style.PinkAppTheme);

        } else if (themeName.equals("purple")) {

            setTheme(R.style.PurpleAppTheme);

        } else if (themeName.equals("default")) {

            setTheme(R.style.DefAppTheme);

        } else if (themeName.equals("")) {

            setTheme(R.style.DefAppTheme);
            preferences.setStringPreference(AppPreferences.APP_THEME, "default");

        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private String getDecimalEquation(String binaryEquation) {
        Stack<String> stack = new Stack<>();
        char c;
        String temp = "";
        for (int i = 0; i < binaryEquation.length(); i++) {
            c = binaryEquation.charAt(i);
            if (isOperator(c)) {
                if (!temp.equals(""))
                    stack.push(temp);
                stack.push(c + "");
                temp = "";
            } else if (c == '(' || c == ')') {
                if (!temp.equals("")) {
                    stack.push(temp);
                    temp = "";
                }
                stack.push(c + "");
            } else {
                temp = temp + c;
            }
        }

        if (!temp.equals(""))
            stack.push(temp);

        Stack<String> abc = new Stack<>();
        while (!stack.empty()) {
            abc.push(stack.pop());
        }

        StringBuilder builder = new StringBuilder();
        while (!abc.empty()) {
            if (isNumber(abc.peek())) {
                builder.append(Integer.parseInt(abc.pop(), 2));
            } else {
                builder.append(abc.pop());
            }
        }

        return builder.toString();
    }

    private static boolean isNumber(String string) {
        return Pattern.matches("-?\\d+(\\.\\d+)?", string);
    }

    private void setKeypadForBinary() {
        //numbers 2 to 9 will be disabled
        b2.setEnabled(false);
        b3.setEnabled(false);
        b4.setEnabled(false);
        b5.setEnabled(false);
        b6.setEnabled(false);
        b7.setEnabled(false);
        b8.setEnabled(false);
        b9.setEnabled(false);
    }

    private void setKeypadForDecimal() {
        //numbers 2 to 9 will be enabled
        b2.setEnabled(true);
        b3.setEnabled(true);
        b4.setEnabled(true);
        b5.setEnabled(true);
        b6.setEnabled(true);
        b7.setEnabled(true);
        b8.setEnabled(true);
        b9.setEnabled(true);
    }

}
