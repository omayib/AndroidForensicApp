package id.co.technomotion.androidforensicapp.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.co.technomotion.androidforensicapp.R;
import id.co.technomotion.androidforensicapp.model.DbFileInfo;
import id.co.technomotion.androidforensicapp.model.PackageInfo;

/**
 * Created by omayib on 5/14/15.
 */
public class PackageAdapter extends BaseExpandableListAdapter{
    private LayoutInflater inflater;
    private int resourceGroup,resourceChild;
    private List<PackageInfo> listOfPackages;
    private HashMap<PackageInfo,List<DbFileInfo>> listOfDatabases;
    private Context context;
    private OnChildCheckedListener onChildCheckedListener;


    public PackageAdapter(Context context,List<PackageInfo> listOfPackages,  int resourceGroup,int resourceChild) {
        this.context = context;
        this.listOfPackages = listOfPackages;
        this.resourceGroup = resourceGroup;
        this.resourceChild=resourceChild;
        this.listOfDatabases=new HashMap<>();
        this.inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public void setChild(PackageInfo parent){
        this.listOfDatabases.put(parent,parent.getDatabaseFiles());
        System.out.println("setChild "+listOfDatabases.get(parent).toString());
    }

    public void setOnChildCheckedListener(OnChildCheckedListener listener){
        this.onChildCheckedListener=listener;
    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    @Override
    public int getGroupCount() {
        return listOfPackages.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(this.listOfDatabases==null)
            return 0;
        if(this.listOfDatabases.get(this.listOfPackages.get(groupPosition))==null)
            return 0;
        return this.listOfDatabases.get(this.listOfPackages.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listOfPackages.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listOfDatabases.get(this.listOfPackages.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder viewHolder;
        PackageInfo packageInfo= (PackageInfo) getGroup(groupPosition);
        if(convertView==null){
            convertView=inflater.inflate(resourceGroup,parent,false);
            viewHolder=new GroupHolder();
            viewHolder.textViewGroup = (TextView) convertView.findViewById(R.id.group);
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (GroupHolder) convertView.getTag();
        }


        viewHolder.textViewGroup.setText(packageInfo.getAppName());
        viewHolder.textViewGroup.setBackgroundColor(Color.GREEN);

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder viewHolder;
        final DbFileInfo dbFileInfo= (DbFileInfo) getChild(groupPosition,childPosition);
        if(convertView==null){
            convertView=inflater.inflate(resourceChild,parent,false);
            viewHolder=new ChildHolder();
            viewHolder.checkBoxChild = (TextView) convertView.findViewById(R.id.child_item_textView);

            viewHolder.checkBoxChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChildCheckedListener.onChildCheckedListener(dbFileInfo.getPath());

                }
            });
            convertView.setTag(viewHolder);
        }else{
            viewHolder= (ChildHolder) convertView.getTag();
        }
        viewHolder.checkBoxChild.setText(dbFileInfo.getName());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface OnChildCheckedListener{
        void onChildCheckedListener(String path);
    }

    private static class GroupHolder{
        TextView textViewGroup;
    }
    private static class ChildHolder{
        TextView checkBoxChild;
    }
}
