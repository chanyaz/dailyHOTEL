package com.twoheart.dailyhotel.screen.common;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;

public class PermissionManagerActivity extends BaseActivity implements Constants
{
    private static final String INTENT_EXTRA_DATA_PERMISSION = "permission";

    private PermissionType mPermissionType;
    private Dialog mDialog;

    public enum PermissionType
    {
        READ_PHONE_STATE,
        ACCESS_FINE_LOCATION
    }

    public static Intent newInstance(Context context, PermissionType permission)
    {
        Intent intent = new Intent(context, PermissionManagerActivity.class);
        intent.putExtra(INTENT_EXTRA_DATA_PERMISSION, permission.name());

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent == null)
        {
            finish(RESULT_CANCELED);
            return;
        }

        if (Util.isOverAPI23() == false)
        {
            finish(RESULT_OK);
            return;
        }

        mPermissionType = PermissionType.valueOf(intent.getStringExtra(INTENT_EXTRA_DATA_PERMISSION));

        switch (mPermissionType)
        {
            case READ_PHONE_STATE:
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                {
                    finish(RESULT_OK);
                    return;
                }
                break;

            case ACCESS_FINE_LOCATION:
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    finish(RESULT_OK);
                    return;
                }
                break;
        }

        initLayout(mPermissionType);
    }

    private void initLayout(PermissionType permissionType)
    {
        setContentView(R.layout.activity_permission_manager);

        View snackDialogLayout = findViewById(R.id.snackDialogLayout);

        switch (permissionType)
        {
            case READ_PHONE_STATE:
            {
                snackDialogLayout.setVisibility(View.VISIBLE);
                showPermissionSnackPopup(snackDialogLayout, permissionType);
                break;
            }

            case ACCESS_FINE_LOCATION:
            {
                snackDialogLayout.setVisibility(View.GONE);
                processCheckPermission(permissionType);
                break;
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        if (mDialog != null && mDialog.isShowing())
        {
            mDialog.dismiss();
        }

        mDialog = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        finish(RESULT_CANCELED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case Constants.REQUEST_CODE_PERMISSIONS_READ_PHONE_STATE:
            case Constants.REQUEST_CODE_PERMISSIONS_ACCESS_FINE_LOCATION:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    finish(RESULT_OK);
                } else
                {
                    showPermissionGuidePopupBySetting(mPermissionType);
                }
                break;
            }

            default:
                finish(RESULT_CANCELED);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case Constants.REQUEST_CODE_PERMISSIONS_READ_PHONE_STATE:
            {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                {
                    finish(RESULT_OK);
                } else
                {
                    finish(RESULT_CANCELED);
                }
                break;
            }
            case Constants.REQUEST_CODE_PERMISSIONS_ACCESS_FINE_LOCATION:
            {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    finish(RESULT_OK);
                } else
                {
                    finish(RESULT_CANCELED);
                }
                break;
            }

            default:
                finish(RESULT_CANCELED);
                break;
        }
    }

    private void finish(int resultCode)
    {
        setResult(resultCode);
        finish();
    }

    private void processCheckPermission(PermissionType permissionType)
    {
        String permission = null;

        switch (permissionType)
        {
            case READ_PHONE_STATE:
                permission = Manifest.permission.READ_PHONE_STATE;
                break;

            case ACCESS_FINE_LOCATION:
                permission = Manifest.permission.ACCESS_FINE_LOCATION;
                break;
        }

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
        {
            if (shouldShowRequestPermissionRationale(permission) == false)
            {
                showPermissionGuidePopup(permissionType);
            } else
            {
                showPermissionGuidePopupBySetting(permissionType);
            }
        }
    }

    private void requestPermissions(PermissionType permissionType)
    {
        switch (permissionType)
        {
            case READ_PHONE_STATE:
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, Constants.REQUEST_CODE_PERMISSIONS_READ_PHONE_STATE);
                break;

            case ACCESS_FINE_LOCATION:
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_CODE_PERMISSIONS_ACCESS_FINE_LOCATION);
                break;
        }
    }

    private void startSettingDetailsActivity(PermissionType permissionType)
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:com.twoheart.dailyhotel"));

        switch (permissionType)
        {
            case READ_PHONE_STATE:
                startActivityForResult(intent, Constants.REQUEST_CODE_PERMISSIONS_READ_PHONE_STATE);
                break;

            case ACCESS_FINE_LOCATION:
                startActivityForResult(intent, Constants.REQUEST_CODE_PERMISSIONS_ACCESS_FINE_LOCATION);
                break;
        }
    }

    public void showPermissionSnackPopup(final View view, final PermissionType permissionType)
    {
        View confirmTextView = view.findViewById(R.id.confirmTextView);
        confirmTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                view.setVisibility(View.GONE);
                processCheckPermission(permissionType);
            }
        });
    }

    public void showPermissionGuidePopup(final PermissionType permissionType)
    {
        mDialog = new Dialog(this);
        mDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(this).inflate(R.layout.view_permission_dialog_layout01, null, false);

        TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        TextView permissionTextView = (TextView) view.findViewById(R.id.permissionTextView);

        switch (permissionType)
        {
            case READ_PHONE_STATE:
                messageTextView.setText(R.string.message_guide_dialog_permission_read_phone_state);
                permissionTextView.setText(R.string.label_phone);
                permissionTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.navibar_ic_call, 0, 0, 0);
                break;

            case ACCESS_FINE_LOCATION:
                messageTextView.setText(R.string.message_guide_dialog_permission_access_fine_location);
                permissionTextView.setText(R.string.label_location);
                permissionTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.navibar_ic_call, 0, 0, 0);
                break;
        }

        View confirmTextView = view.findViewById(R.id.confirmTextView);

        confirmTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                requestPermissions(permissionType);
            }
        });

        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                finish(RESULT_CANCELED);
            }
        });

        try
        {
            mDialog.setContentView(view);
            mDialog.show();
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    public void showPermissionGuidePopupBySetting(final PermissionType permissionType)
    {
        mDialog = new Dialog(this);
        mDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(this).inflate(R.layout.view_permission_dialog_layout02, null, false);

        TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        TextView permissionTextView = (TextView) view.findViewById(R.id.permissionTextView);

        switch (permissionType)
        {
            case READ_PHONE_STATE:
                messageTextView.setText(R.string.message_guide_dialog_denied_permission_read_phone_state);
                permissionTextView.setText(R.string.label_phone);
                permissionTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.navibar_ic_call, 0, 0, 0);
                break;

            case ACCESS_FINE_LOCATION:
                messageTextView.setText(R.string.message_guide_dialog_denied_permission_access_fine_location);
                permissionTextView.setText(R.string.label_location);
                permissionTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.navibar_ic_call, 0, 0, 0);
                break;
        }

        View positiveTextView = view.findViewById(R.id.positiveTextView);
        View negativeTextView = view.findViewById(R.id.negativeTextView);

        positiveTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startSettingDetailsActivity(permissionType);
            }
        });

        negativeTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish(RESULT_CANCELED);
            }
        });

        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                finish(RESULT_CANCELED);
            }
        });

        try
        {
            mDialog.setContentView(view);
            mDialog.show();
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }
}
