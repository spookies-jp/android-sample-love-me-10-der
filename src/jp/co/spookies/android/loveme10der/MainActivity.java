package jp.co.spookies.android.loveme10der;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.spookies.android.loveme10der.model.CallLogData;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

/**
 * MainActivity
 */
public class MainActivity extends ListActivity {
    // 設定メニューのメニュー番号
    private static final int MENU_SETTING = Menu.FIRST;
    // 何日前までの記録を対象とするか（デフォルト値）
    private static final String DEFAULT_DAY_AGO = "30";
    // ランキングMAX表示件
    private static final int LIST_LIMIT = 10;
     // 発着信全てのランキングモード
    private static final int ALL_CALL_TYPE = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setCallLogList(ALL_CALL_TYPE);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        setCallLogList(ALL_CALL_TYPE);
    }

    /**
     * ランキングリスト行をセット
     * 
     * @param callType
     */
    private void setCallLogList(int callType) {
        List<CallLogData> calls = getLongCalls(callType);
        Map<String, CallLogData> callMap = new HashMap<String, CallLogData>();
        // 同一発着信からの通話時間をまとめる
        for (CallLogData log : calls) {
            if (!callMap.containsKey(log.getNumber())) {
                callMap.put(log.getNumber(), log);
            } else {
                CallLogData entryLog = callMap.get(log.getNumber());
                if (entryLog.getCachedName() == null
                        && log.getCachedName() != null) {
                    entryLog.setCachedName(log.getCachedName());
                }
                // 通話時間を足す
                entryLog.addDuration(log.getDuration());
            }
        }

        ArrayList<CallLogData> callList = new ArrayList<CallLogData>(
                callMap.values());
        // 通話時間が長い順に並び替え
        Collections.sort(callList, new CallLogDurationComparator());
        // 指定件数に絞る
        if (callList.size() > LIST_LIMIT) {
            callList.subList(LIST_LIMIT, callList.size()).clear();
        }
        setListAdapter(new CallLogAdapter(this, callList));

        // タイトル画像の入れ替え
        setTitleImage(callType);
    }

    /**
     * タイトル画像の入れ替え
     * 
     * @param callType
     */
    private void setTitleImage(int callType) {
        ImageView titleImage = (ImageView) findViewById(R.id.titleImage);
        if (callType == ALL_CALL_TYPE) {
            // トータル
            titleImage.setImageResource(R.drawable.top_total);
        } else if (callType == CallLog.Calls.INCOMING_TYPE) {
            // 着信
            titleImage.setImageResource(R.drawable.top_receipt);
        } else if (callType == CallLog.Calls.OUTGOING_TYPE) {
            // 発信
            titleImage.setImageResource(R.drawable.top_send);
        }
    }

    /**
     * 一定期限内の全通話データを取得
     * 
     * @param callType
     */
    private List<CallLogData> getLongCalls(int callType) {
        // 取得項目
        String[] projection = new String[] { CallLog.Calls.TYPE,
                CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER,
                CallLog.Calls.DURATION, };
        // 条件文
        String selection = CallLog.Calls.DATE + " >= ? ";
        if (callType != ALL_CALL_TYPE) {
            selection += " AND " + CallLog.Calls.TYPE + " = ? ";
        }

        // メニューの設定画面で設定した期間から、ログ解析期間を計算
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, -getPeriod());
        Date date = cal.getTime();
        // 条件値
        String[] sectionArgs;
        if (callType != ALL_CALL_TYPE) {
            sectionArgs = new String[2];
            sectionArgs[0] = String.valueOf(date.getTime());
            sectionArgs[1] = String.valueOf(callType);
        } else {
            sectionArgs = new String[1];
            sectionArgs[0] = String.valueOf(date.getTime());
        }
        // ソート順
        String order = CallLog.Calls.NUMBER + " asc";
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
                projection, selection, sectionArgs, order);
        if (cursor == null) {
            return null;
        }

        cursor.moveToFirst();
        List<CallLogData> logs = new ArrayList<CallLogData>();
        // 結果行を一つずつ取りだす
        for (int i = 0; i < cursor.getCount(); i++) {
            String type = cursor.getString(cursor
                    .getColumnIndexOrThrow(CallLog.Calls.TYPE));
            String cachedName = cursor.getString(cursor
                    .getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
            String number = cursor.getString(cursor
                    .getColumnIndexOrThrow(CallLog.Calls.NUMBER));
            Integer duration = cursor.getInt(cursor
                    .getColumnIndexOrThrow(CallLog.Calls.DURATION));
            CallLogData log = new CallLogData();
            log.setType(type);
            log.setCachedName(cachedName);
            log.setNumber(number);
            log.setDuration(duration);
            logs.add(log);
            cursor.moveToNext();
        }
        cursor.close();

        return logs;
    }

    /**
     * トータルランキング
     * 
     * @param v
     */
    public void onClickTotalButton(View v) {
        setCallLogList(ALL_CALL_TYPE);
    }

    /**
     * 着信ランキング
     * 
     * @param v
     */
    public void onClickReceiptButton(View v) {
        setCallLogList(CallLog.Calls.INCOMING_TYPE);
    }

    /**
     * 発信ランキング
     * 
     * @param v
     */
    public void onClickSendButton(View v) {
        setCallLogList(CallLog.Calls.OUTGOING_TYPE);
    }

    /**
     * メニュー画面
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_SETTING, Menu.NONE, R.string.period_title)
                .setIcon(android.R.drawable.ic_menu_agenda);
        return super.onCreateOptionsMenu(menu);

    }

    /**
     * メニュー画面選択時の処理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // パーツ選択
        case MENU_SETTING:
            selectSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 設定画面のActivityを呼び出し
     */
    private void selectSettings() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    /**
     * 何日前までの記録を解析するか
     */
    private Integer getPeriod() {
        String period = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext()).getString(
                getResources().getString(R.string.period_key), DEFAULT_DAY_AGO);
        return Integer.parseInt(period);
    }

    
    /**
     * 発着信Listデータのソート用クラス
     */
    class CallLogDurationComparator implements Comparator<CallLogData> {
        public static final int ASC = 1; // 昇順
        public static final int DESC = -1; // 降順

        public CallLogDurationComparator() {
        }

        // 通話時間の降順に並び替え
        public int compare(CallLogData arg0, CallLogData arg1) {
            return (((CallLogData) arg0).getDuration() - ((CallLogData) arg1)
                    .getDuration()) * DESC;
        }
    }
    
}