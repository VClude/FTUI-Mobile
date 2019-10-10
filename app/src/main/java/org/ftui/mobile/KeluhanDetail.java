package org.ftui.mobile;

import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ftui.mobile.fragment.ComplaintComments;
import org.ftui.mobile.fragment.ComplaintDescription;
import org.ftui.mobile.fragment.Home;
import org.ftui.mobile.model.CompleteUser;
import org.ftui.mobile.model.User;
import org.ftui.mobile.model.keluhan.Comment;
import org.ftui.mobile.model.keluhan.Ticket;
import org.ftui.mobile.model.surveyor.Details;
import org.ftui.mobile.model.surveyor.Surveyor;
import org.ftui.mobile.model.surveyor.SurveyorResponse;

import java.lang.reflect.Type;
import java.util.*;

public class KeluhanDetail extends AppCompatActivity implements
        ComplaintDescription.OnFragmentInteractionListener,
        ComplaintComments.OnFragmentInteractionListener {


    private LinearLayout parentSwitcher;
    private LinearLayout complaintDetailSwitcher;
    private LinearLayout commentSwitcher;
    private Ticket keluhan_data;
    private ArrayList<Comment> keluhan_comment;
    Boolean switcherStateAtComplaintDetail = true;
    TransitionDrawable complaintDetailTransDrawable;
    TransitionDrawable commentTransDrawable;
    CompleteUser user;
    User tokenUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keluhan_detail);
        Intent i = getIntent();

        keluhan_data = (Ticket) i.getSerializableExtra("keluhan_data");
        List<Comment> comment = keluhan_data.getComments();
        keluhan_comment = new ArrayList<>(comment.size());
        keluhan_comment.addAll(comment);
        String baseImgUrl = i.getStringExtra("baseImgUrl");

        Gson gson = new Gson();
        tokenUser = gson.fromJson(getSharedPreferences(LoginActivity.USER_SHARED_PREFERENCE, MODE_PRIVATE).getString("user", null), User.class);
        user = gson.fromJson(getSharedPreferences(Home.COMPLETE_USER_SHARED_PREFERENCES, MODE_PRIVATE).getString("complete_user", null), CompleteUser.class);


        Fragment fr = ComplaintDescription.newInstance(keluhan_data, baseImgUrl);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.complaint_detail_main_fragment, fr, ComplaintDescription.COMPLAINT_DESCRIPTION_FRAGMENT_TAG)
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.komplaint);


        complaintDetailSwitcher = findViewById(R.id.complaint_desc_switcher);
        commentSwitcher = findViewById(R.id.comments_switcher);

        complaintDetailTransDrawable = (TransitionDrawable) complaintDetailSwitcher.getBackground();
        commentTransDrawable = (TransitionDrawable) commentSwitcher.getBackground();

        complaintDetailTransDrawable.startTransition(300);

        View.OnClickListener mHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("OnClick", "Clicked" + v.getTag());
                switch (v.getId()){
                    case R.id.complaint_desc_switcher :
                        if(!switcherStateAtComplaintDetail){
                            commentTransDrawable.startTransition(0);
                            commentTransDrawable.reverseTransition(300);
                            complaintDetailTransDrawable.startTransition(300);
                            switcherStateAtComplaintDetail = true;

                            getSupportFragmentManager().popBackStack();
                        }
                        break;
                    case R.id.comments_switcher :
                        if(switcherStateAtComplaintDetail){
                            complaintDetailTransDrawable.startTransition(0);
                            complaintDetailTransDrawable.reverseTransition(300);
                            commentTransDrawable.startTransition(300);
                            switcherStateAtComplaintDetail = false;

                            Fragment fr = ComplaintComments.newInstance(keluhan_comment);

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.complaint_detail_main_fragment, fr, ComplaintComments.COMPLAINT_COMMENTS_FRAGMENT_TAG)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        break;
                }
            }
        };

        complaintDetailSwitcher.setOnClickListener(mHandler);
        commentSwitcher.setOnClickListener(mHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(getSharedPreferences(Home.COMPLETE_USER_SHARED_PREFERENCES, MODE_PRIVATE).contains("complete_user")){
            Gson gson = new Gson();
            CompleteUser user = gson.fromJson(getSharedPreferences(Home.COMPLETE_USER_SHARED_PREFERENCES, MODE_PRIVATE).getString("complete_user", null), CompleteUser.class);
            if(LoginActivity.isSurveyor(this)){
                String spSurveyor = getSharedPreferences(Home.SURVEYOR_SHARED_PREFERENCES, MODE_PRIVATE).getString("surveyor", null);

                Type listType = new TypeToken<List<Surveyor>>() {}.getType();
                List<Surveyor> surveyors = gson.fromJson(spSurveyor, listType);

                for(Surveyor surveyor : surveyors){
                    Details det = surveyor.getDetails();
                    if(det.getName().equals(keluhan_data.getCategory().getName())){
                        getMenuInflater().inflate(R.menu.keluhan_detail_activity_context_menu, menu);
                        MenuItem item = menu.getItem(0);
                        item.setTitle(evalStatusToOptionMenuString(keluhan_data.getStatus().getName()));
                        break;
                    }
                }

            }else if(user.getId() == keluhan_data.getUser().getId()){
                getMenuInflater().inflate(R.menu.keluhan_detail_activity_context_menu_user, menu);

            }else if(keluhan_data.getStatus().getName().equals("FINISHED")){
                getMenuInflater().inflate(R.menu.keluhan_detail_activity_context_menu, menu);
                MenuItem item = menu.getItem(0);
                item.setTitle(R.string.FINISHED_OM);
            }
        }
        return true;
    }

    public static int evalStatusToOptionMenuString(String status){
        int humanReadableStringResId;
        switch (status){
            case "AWAITING_FOLLOWUP" :
                humanReadableStringResId = R.string.AWAITING_FOLLOWUP_OM;
                break;
            case "IS_BEING_FOLLOWED_UP" :
                humanReadableStringResId = R.string.IS_BEING_FOLLOWED_UP_OM;
                break;
            case "FINISHED":
                humanReadableStringResId = R.string.FINISHED_OM;
                break;
            case "REOPENED":
                humanReadableStringResId = R.string.IS_BEING_FOLLOWED_UP_OM;
                break;
            default:
                humanReadableStringResId = R.string.AWAITING_FOLLOWUP_OM;
        }

        return humanReadableStringResId;
    }

    public static String evalStatusToAPIMethod(String status){
        String method;
        switch (status){
            case "IS_BEING_FOLLOWED_UP" :
            case "REOPENED":
                method = "complete";
                break;
            case "FINISHED":
                method = "reopen";
                break;
            default:
                method = "process";
        }

        return method;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.elevate_complaint_status:
                HashMap<String,String> headerMap = new HashMap<>();
                headerMap.put("accept", "application/json");
                headerMap.put("Authorization", "Bearer " + tokenUser.getToken());

                String url = buildProcessUrl(evalStatusToAPIMethod(keluhan_data.getStatus().getName()), keluhan_data.getId());
                Log.d("TAG", "buildedURL: " + url);

                break;
            case R.id.delete_complaint:
                break;
            case R.id.delete_complaint_user:
                break;
        }
        return true;
    }

    public static String buildProcessUrl(String method, Integer id){

        return "http://pengaduan.ccit-solution.com/api/keluhan/" + method + "/" + id;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            finish();
        }else if(!switcherStateAtComplaintDetail){
            commentTransDrawable.startTransition(0);
            commentTransDrawable.reverseTransition(300);
            complaintDetailTransDrawable.startTransition(300);
            switcherStateAtComplaintDetail = true;

            getSupportFragmentManager().popBackStack();

        }else if(switcherStateAtComplaintDetail){
            complaintDetailTransDrawable.startTransition(0);
            complaintDetailTransDrawable.reverseTransition(300);
            commentTransDrawable.startTransition(300);
            switcherStateAtComplaintDetail = false;

            getSupportFragmentManager().popBackStack();
        }
    }

    private FragmentManager.OnBackStackChangedListener getListener(){
        FragmentManager.OnBackStackChangedListener result = new FragmentManager.OnBackStackChangedListener(){
            public void onBackStackChanged(){
                FragmentManager manager = getSupportFragmentManager();

                if (manager != null){
                    ComplaintDescription currFrag = (ComplaintDescription) manager.findFragmentById(R.id.complaint_detail_main_fragment);

                    currFrag.onFragmentResume();
                }
            }
        };

        return result;
    }

    @Override
    public void onFragmentInteraction(Uri uri){

    }
}
