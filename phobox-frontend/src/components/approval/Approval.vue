<template>
  <div id="approval">
        <!-- Show selected picture -->
        <div class="picPanel">
            <transition name="fadeIn">
                <img :src="SERVER_PATH + selectedPic" id="mainPic" 
                    :data-zoom-image="selectedPic"
                    :key="selectedPic" 
                    v-if="selectedPic" 
                    class="selectedPic" />
            </transition>
        </div>

        <!-- List all elements -->
        <div class="pictureNavigation">
            <ul>
                <li v-for="(pic, key) in pictures" :key="key">
                    <img :src="SERVER_PATH + pic" class="preview" @click="onPictureSelect(pic)" v-bind:class="{ isSelected: pic === selectedPic }" />
                </li>
            </ul>
        </div>
       
        <!-- Accept/Decline panel-->
        <div class="judgepanel">
            <!-- Accept -->
            <div class="judgeButton green" @click="onAccept">
                <i class="fa fa-check-circle" aria-hidden="true"></i>
                {{ $t('approval.accept') }}
                </div>
                
                <div class="spacer"></div>

                <!-- Decline -->
                <div class="judgeButton red" @click="onDecline">
                <i class="fa fa-trash" aria-hidden="true"></i>
                {{ $t('approval.decline') }}
            </div>
        </div>

    </div>
</template>

<script>
import ComService from '@/utils/ComService';
import ProductZoomer from 'vue-product-zoomer'

export default {
  name: "Approval",
  props: [],
  components: {
    ProductZoomer
  },
  data() {
    return {
      pictures: [],
      selectedPic: null,
      status: null,
      images: {
        normal_size: [
        ]
      },
      zoomerOptions: {
        'zoomFactor': 4,
        'pane': 'container',
        'hoverDelay': 300,
        'namespace': 'container-zoomer',
        'move_by_click':true,
        'scroll_items': 4,
        'choosed_thumb_border_color': "#ff3d00"
      },
      SERVER_PATH: process.env.SERVER_PATH
    };
  },
  methods: {
    init() {
      new ComService().getApprovalPictures(data => {
        this.pictures = data;
        this.selectedPic = this.pictures.length > 0 ? this.pictures[0] : null;
        // this.setMagnification();
      });
    },

    onPictureSelect(pic) {
      this.selectedPic = pic;
      this.images.normal_size = [
        {'id': this.selectedPic, 'url': this.selectedPic}
      ]

      // Activate maginfication view
//      this.setMagnification();
    },

    setMagnification() {
      if (this.selectedPic === null) return;

      setTimeout(() => {
        $("#mainPic").elevateZoom({
          zoomType: "lens",
          zoomLens: false,
          lensShape: "square",
          lensSize: 300
        });
      }, 500);
    },

    onAccept() {
      if (this.selectedPic === null) return;

      let pathWithOutExtPrefix = this.selectedPic.substr(3, this.selectedPic.length);
      new ComService().acceptApprovalPicture(pathWithOutExtPrefix, data => {
        this.status = data.status;
        this.init();
      });
    },

    onDecline() {
      if (this.selectedPic === null) return;

      let pathWithOutExtPrefix = this.selectedPic.substr(3, this.selectedPic.length);
      new ComService().declineApprovalPicture(pathWithOutExtPrefix, data => {
        this.status = data.status;
        this.init();
      });
    },

    next() {
      if (this.hasNext) {
        let index = this.pictures.indexOf(this.selectedPic);
        this.selectedPic = this.pictures[index + 1];
        this.scrollToPreview();
      }
    },

    previous() {
      if (this.hasPrevious) {
        let index = this.pictures.indexOf(this.selectedPic);
        this.selectedPic = this.pictures[index - 1];
        this.scrollToPreview();
      }
    },

    stopZoom() {
      $.removeData($("#mainPic"), "elevateZoom");
      $(".zoomContainer").remove();
    }
  },
  computed: {
    hasNext: function() {
      let index = this.pictures.indexOf(this.selectedPic);
      return index + 1 < this.pictures.length;
    },

    hasPrevious: function() {
      let index = this.pictures.indexOf(this.selectedPic);
      return index - 1 >= 0;
    }
  },
  watch: {},
  created() {
    this.init();

    // Add cursor key listeners for navigation
    document.onkeydown = (e) => {
      e = e || window.event;

      // Right/Next
      if (e.keyCode === 39 && this.hasNext) {
        this.next();
      }

      // Left/Previous
      if (e.keyCode === 37 && this.hasPrevious) {
        this.previous();
      }
    };
  },
  beforeDestroy() {
    this.stopZoom();
  }
};
</script>

<style>
#approval {
  margin: 15px;
  margin-top: -40px;
  height: 100%;
}

#approval .pictureNavigation {
  height: 200px;
  background-color: #2b2b2b;
  padding: 10px;

  overflow: hidden;
  overflow-x: visible;
  overflow-y: hidden;

  white-space: nowrap;
}

#approval ul {
  white-space: nowrap;
  padding: 0px;
}

#approval ul li {
  display: inline-block;
}

#approval img.isSelected {
  border: 3px solid;
  border-color: #337ab7bf;
}

#approval img.preview {
  position: relative;
  float: left;
  margin-right: 5px;
  border-radius: 3px;
  max-width: 240px;
  max-height: 160px;
  transition: 0.3s border;
}

#approval .picPanel {
  margin-bottom: 10px;
  height: calc(100vh - 310px);
  text-align: center;
}

#approval img.selectedPic {
  border-radius: 3px;
  max-width: 100%;
  max-height: 100%;
  vertical-align: middle;
  transition: 0.3s all;
}

#approval .judgepanel {
  position: fixed;
  bottom: 0px;
  width: 100%;
  background-color: #191717;
  margin-left: -15px;
}

#approval .judgeButton {
  float: left;
  width: 50%;
  text-align: center;
  padding: 10px;
  cursor: pointer;
  transition: 0.3s background-color;
}

#approval .judgeButton:hover {
  background-color: #262626;
}

#approval .judgepanel .spacer {
  width: 2px;
  height: 100%;
  position: absolute;
  background-color: #272525;
  left: 50%;
}

.green {
  color: #5cba5e;
}

.red {
  color: #a94442;
}

/* Animations */
.fadeIn-enter-active {
  transition: opacity 0.3s;
}

.fadeIn-enter {
  opacity: 0;
}

.fadeIn-leave-to,
.fadeIn-leave-active {
  display: none;
  transition: none;
  opacity: 0;
}
</style>
