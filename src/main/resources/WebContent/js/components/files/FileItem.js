const FileItem = Vue.component(
	'fileitem', {
    template: `
    <div class="item_area">
        
        <div class="menu_button" v-on:click="toggleMenu()">
            <i class="fa fa-caret-square-o-down" aria-hidden="true" :id="item.name"></i>
        </div>

        <div class="item" v-on:click="open(item)" :id="encodePath">
            <!-- File: Show-->
            <img class="item_thumb" 
                :src="item.thumb"
                v-if="item.type === 'file'" />

            <p v-if="item.thumb==='img/stopwatch.png'" class="reloadInfo">
                {{ Locale.values.pictures.waitforthumb }}
            </p>

            <!-- Directory: Show preview if exists-->
            <img class="item_thumb" 
                :src="item.preview"
                v-on:click="open(item)"
                v-if="item.type === 'dir' && item.preview != null" />
        
            <!-- Directory: Show static image if preview not exists-->
            <img class="item_thumb_static" 
                src="img/directory.png"
                v-on:click="open(item)"                
                v-if="item.type === 'dir' && item.preview == null" />

            <!-- Directory: Show name -->
            <div class="item_name" v-if="item.type === 'dir'" v-on:click="open(item)">
                <i class="fa fa-folder"></i>
                <span>{{ item.name }}</span>
            </div>
        </div>
        
        <transition name="fade">
            <div class="menu" v-show="menuShow" v-click-outside="onClickOutside">
                <ul>
                    <li class="menu_element" v-on:click="closeMenu()">
                        <a class="downloadDirectory" :href="downloadPath()" target="_blank" download>
                            <i class="fa fa-download" aria-hidden="true"></i> {{ Locale.values.pictures.download }}
                        </a>
                    </li>
                    <li class="menu_element" v-on:click="onRename"><i class="fa fa-pencil" aria-hidden="true"></i> {{ Locale.values.pictures.rename }}</li>
                    <li class="menu_element" v-on:click="onDelete"><i class="fa fa-trash" aria-hidden="true"></i> {{ Locale.values.pictures.delete }}</li>
                    <li class="menu_element" v-on:click="onTags"><i class="fa fa-tags" aria-hidden="true"></i> {{ Locale.values.pictures.tags }}</li>
                </ul>
            </div>
        </transition>
	</div>
    `,
    props: ['item'],
    data: function() {
    	return {
            menuShow: false,
            contextMenuItem: undefined,
            Locale: Locale,
        };
    },
    methods: {
        open : function(item) {
            // Set selected item to parent component
            // if it's a file, otherwise open directory
            if(item.type === 'file') {
                this.$parent.selectedItem = item;
            } else {
                path = new ComService().encodePath(item.path);
                router.push({ path: "/photos/"+path });
            }
        },

        downloadPath: function() {
            
            if(this.item.type === "dir") {
                com = new ComService();
                return "api/photos/download/" + com.encodePath(this.item.path);
            
            } else {

                return this.item.path;
            }
        },
        
        toggleMenu: function(id) {
            this.menuShow = !this.menuShow; 
        },

        closeMenu: function() {
            this.menuShow = false;
        },

        onClickOutside: function(e) {
            // Hide menu if no element is clickt and the contextmenu is open
            if((e.srcElement.id === "" || e.srcElement.id === "filebrowser") && this.menuShow) {
                this.closeMenu();
            }
        },

        onDownload: function() {
            this.closeMenu();
        },

        onRename: function() {
            this.$parent.renameItem = this.item;
            this.closeMenu();
        },
        
        onDelete: function() {
            this.$parent.deleteItem = this.item;
            this.closeMenu();
        },

        onTags: function() {
            this.$parent.tagsItem = this.item;
            this.closeMenu();
        },
    },
    computed: { 
        encodePath: function() {
            return (this.item.path).replace(/\//g , "_");
        },
    },

    created: function() {
    }
});