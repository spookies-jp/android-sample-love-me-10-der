package jp.co.spookies.android.loveme10der;

import java.util.List;

import jp.co.spookies.android.loveme10der.model.CallLogData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 結果一覧のリストViewカスタマイズクラス
 */
public class CallLogAdapter extends ArrayAdapter<CallLogData> {

    private LayoutInflater infrater;
    private ImageView rankImageView;
    private TextView rankView;
    private TextView nameView;
    private TextView durationView;

    public CallLogAdapter(Context context, List<CallLogData> logs) {
        super(context, 0, logs);
        infrater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 行を構成
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (convertView == null) {
            view = infrater.inflate(R.xml.item_row, null);
        }

        // 現在の行にある通話ログを取得
        final CallLogData log = this.getItem(position);
        if (log != null) {
            rankImageView = (ImageView) view.findViewById(R.id.item_rank_image);
            // 上位３位まで旗アイコンを表示
            if (position >= 3) {
                rankImageView.setVisibility(View.INVISIBLE);
            } else {
                rankImageView.setVisibility(View.VISIBLE);
            }
            // ランク
            rankView = (TextView) view.findViewById(R.id.item_rank);
            rankView.setText(String.valueOf(position + 1));

            // 対象相手
            nameView = (TextView) view.findViewById(R.id.item_name);
            if (log.getCachedName() == null || log.getCachedName().equals("")) {
                nameView.setText(log.getNumber());
            } else {
                nameView.setText(log.getCachedName());
            }

            // 通話時間
            durationView = (TextView) view.findViewById(R.id.item_duration);
            // 時間をh:m:s表示
            Integer h = log.getDuration() / 3600;
            Integer m = log.getDuration() % 3600 / 60;
            Integer s = log.getDuration() % 3600 % 60;
            durationView.setText(h.toString() + "h " + m.toString() + "m "
                    + s.toString() + "s");

            // 行クリック時のイベントリスナー
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 電話をかける画面の呼び出し
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri
                            .parse("tel:" + log.getNumber()));
                    v.getContext().startActivity(intent);
                }
            });
        }
        return view;
    }
}
