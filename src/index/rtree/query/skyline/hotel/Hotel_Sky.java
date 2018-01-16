package index.rtree.query.skyline.hotel;

import index.basic_ds.SortedLinList;
import index.rtree.dimitris.Data;
import index.rtree.dimitris.DirEntry;
import index.rtree.dimitris.RTDataNode;
import index.rtree.dimitris.RTDirNode;
import index.rtree.dimitris.RTNode;
import index.rtree.dimitris.RTree;
import index.rtree.query.Skyline;
import index.rtree.query.event.Skyline_Query_Event;
import index.rtree.query.skyline.HeapEntry;
import index.rtree.query.skyline.Hotel_Dir_Entry;
import index.rtree.query.skyline.Hotel_Entry;

import javax.swing.JPanel;


import com.skyline.global.MyConstants;


public class Hotel_Sky extends Skyline{
	
	public Hotel_Sky(RTree tree, JPanel target, Skyline_Query_Event query, long time) {

		super(tree, target, query, time);
	}
	
	

	public SortedLinList h_skyline() {

		tree.load_root();
		
		RTNode root_ptr = tree.root_ptr;
	
		SortedLinList Result_Set = new SortedLinList();
		SortedLinList heap = new SortedLinList();
		
		if(tree.root_is_data)
		{
		}
		
		else{
			
			RTDirNode node = (RTDirNode) root_ptr;
			
			DirEntry[] entries = node.entries;
			
			for (int i = 0; i < node.get_num(); i++) {
				DirEntry entry = entries[i];
				heap.insert(new Hotel_Dir_Entry(entry.get_son(), point));
			}
		}
				
		heap.sort();
		
		while(heap.get_num() > 0)
		{
			HeapEntry obj = (HeapEntry) heap.get_first(); heap.erase();
						
			if( MyConstants.isDominated_hotel(point,obj.mbr, Result_Set)) continue;
			
			else{
				
				if(obj instanceof Hotel_Entry){
					Hotel_Entry hotel_entry = (Hotel_Entry) obj;					
					hotel_entry.data.distanz = hotel_entry.dist;
					Result_Set.insert(obj);
					
					Hotel hotel = (Hotel) (hotel_entry.data);
					hotel.selected = true;
					
					sleep_for_a_while(sleep_time);
					target.repaint();
				}
				
				else{
					
					Hotel_Dir_Entry entry = (Hotel_Dir_Entry) obj;
					if(entry.isDataNode)
					{
						RTDataNode node = (RTDataNode)entry.node;
						
						Data[] data = node.data;
						for (int i = 0; i < node.get_num(); i++) {
							Data child = data[i];
							
							if(MyConstants.isDominated_hotel(point,child.data, Result_Set) ) continue;
							
							else heap.insert(new Hotel_Entry(child, point) );
						}
							
					}
					
					else{
						
						RTDirNode node = (RTDirNode)entry.node;
						
						DirEntry[] entries = node.entries;
						for (int i = 0; i < node.get_num(); i++) {
							
							DirEntry child = entries[i];
							
							if(child.son_is_data)
							{
								RTDataNode child_ptr = (RTDataNode) child.get_son();
								
								if( MyConstants.isDominated_hotel(point,child_ptr.get_mbr(), Result_Set) ) continue;
								
								else
									heap.insert(new Hotel_Dir_Entry(child_ptr, point));
							}
							
							else{
								
								RTDirNode child_ptr = (RTDirNode) child.get_son();
								
								if( MyConstants.isDominated_hotel(point,child_ptr.get_mbr(), Result_Set) ) continue;
								
								else
									heap.insert(new Hotel_Dir_Entry(child_ptr, point));
								
							}
						}
						
					}
					
				heap.sort();	
				}
			}

		}
		
		Result_Set.sort();

		return Result_Set;
	}

	
	@Override
	public void run() {
		h_skyline();
	}

	
	
}
