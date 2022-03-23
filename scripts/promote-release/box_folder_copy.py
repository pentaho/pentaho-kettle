from boxsdk import JWTAuth,Client
import argparse,os

def get_folder_id(client,parent_folder_id,folder_name):
    curr_folder = client.folder(folder_id=parent_folder_id).get()
    items = curr_folder.get_items(limit=100, offset=0)
    for item in items:
        if folder_name == item.name:
            return item.id

def get_req_folder(client,folder_path):
    try:
        folders = folder_path.split("/")          
        base_folder_id = get_folder_id(client,"0",folders.pop(0))
        for x in range(0,len(folders)):
            curr_folder_id = get_folder_id(client,base_folder_id,folders[x])
            base_folder_id = curr_folder_id
        return curr_folder_id
    except Exception as e:
        print("Incorrect Path or Path not found:{0}".format(folder_path))
        raise RuntimeError(e)                  

def copy_folder(client,src_folder_id,dest_folder_id,target_folder_name):
    try:
        folder_to_copy = client.folder(src_folder_id)
        destination_folder = client.folder(dest_folder_id)    
        folder_copy = folder_to_copy.copy(destination_folder,name=target_folder_name)
        new_folder_id = folder_copy.id
        return new_folder_id
    except Exception as e:
        print("Could not perform required copy due to below error..")
        raise RuntimeError(e) 

def create_shared_link(folder_id):
    try:
        shared_link = client.folder(folder_id).get_shared_link()
        return shared_link
    except Exception as e:
        print("Error creating shared link due to below error..")
        raise RuntimeError(e)

def is_folder_exists(client,parent_folder_id,folder_name):
    curr_folder = client.folder(folder_id=parent_folder_id).get()
    items = curr_folder.get_items(limit=100, offset=0)
    for item in items:
        if folder_name == item.name:
            return True
    return False

def main(src_path,dest_path,target_folder_name,dryrun):

    src_path = src_path.rstrip('/')
    dest_path = dest_path.rstrip('/')
    src_folder_id = get_req_folder(client,src_path)
    dest_folder_id = get_req_folder(client,dest_path)
    # print("Source Folder Id: {0}".format(src_folder_id))
    # print("Destination Folder Id: {0}".format(dest_folder_id))

    if(is_folder_exists(client,dest_folder_id,target_folder_name)):
        target_folder_name = target_folder_name + "-" + os.environ.get("BUILD_ID","999")
    if not dryrun:
        new_folder_id = copy_folder(client,src_folder_id,dest_folder_id,target_folder_name)
        # print("Folder {0} is copied and the new path is {1}".format(src_path,dest_path+"/"+target_folder_name))
        shared_link = create_shared_link(new_folder_id)
        # print("Shared link for the new folder: {0}".format(shared_link))
        print(shared_link)
    # else:
    #     print("######  Dry Run  ######")
    #     print("Folder {0} will be copied and the new path would be {1}".format(src_path,dest_path+"/"+target_folder_name))    

if __name__ == '__main__':
    """
    Script to copy a box folder to another

    Example Invocation:
    python3 box_folder_copy.py <path>/boxguy_config.json CI/9.1-QAT/587/ buildguy/Srini/ cust-release
    """

    parser = argparse.ArgumentParser()
    parser.add_argument("config_path", help="Path to box config file")
    parser.add_argument("src_path", help="Source folder path on Box")
    parser.add_argument("dest_path", help="Destianation folder path on Box")
    parser.add_argument("target_folder_name", help="Destianation folder name on Box")
    parser.add_argument("--dryrun", "--dry-run", dest='dryrun', default=False, action='store_true', help="dry run the copy")
    args = parser.parse_args()

    config = JWTAuth.from_settings_file(args.config_path)
    client = Client(config)
    user = client.user('me').get()
    # print("Authenticated User: {0}".format(user.name))
    main(args.src_path,args.dest_path,args.target_folder_name,args.dryrun)