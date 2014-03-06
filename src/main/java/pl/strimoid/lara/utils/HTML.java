package pl.strimoid.lara.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Annotation;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.QuoteSpan;
import android.util.Log;

import org.xml.sax.XMLReader;

public class HTML implements Html.TagHandler {

    public Spanned parse(String text) {
        text = text.replaceAll("<blockquote>", "<quote>");
        text = text.replaceAll("</blockquote>", "</quote>");

        return Html.fromHtml(text, null, this);
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if(tag.equalsIgnoreCase("quote"))
            processQuote(opening, (SpannableStringBuilder) output);
    }

    private void processQuote(boolean opening, SpannableStringBuilder text) {
        if (opening) {
            handleP(text);
            start(text, new Quote());
        } else {
            handleP(text);
            end(text, Quote.class, new MyQuoteSpan());
        }
    }

    private static Object getLast(Spanned text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0)
            return null;
        else
            return objs[objs.length - 1];
    }

    private static void start(SpannableStringBuilder text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void end(SpannableStringBuilder text, Class kind, Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len)
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void handleP(SpannableStringBuilder text) {
        int len = text.length();

        if (len >= 1 && text.charAt(len - 1) == '\n') {
            if (len >= 2 && text.charAt(len - 2) == '\n') {
                return;
            }

            text.append("\n");
            return;
        }

        if (len != 0) {
            text.append("\n\n");
        }
    }

    private static class Quote {}

    private class MyQuoteSpan extends QuoteSpan {

        private static final int STRIPE_WIDTH = 8;
        private static final int GAP_WIDTH = 8;

        private final int mColor;

        public MyQuoteSpan() {
            super();
            mColor = 0xffdddddd;
        }

        public int getLeadingMargin(boolean first) {
            return STRIPE_WIDTH + GAP_WIDTH;
        }

        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                      int top, int baseline, int bottom,
                                      CharSequence text, int start, int end,
                                      boolean first, Layout layout) {
            Paint.Style style = p.getStyle();
            int color = p.getColor();

            p.setStyle(Paint.Style.FILL);
            p.setColor(mColor);

            c.drawRect(x, top, x + dir * STRIPE_WIDTH, bottom, p);

            p.setStyle(style);
            p.setColor(color);
        }

    }
}
